# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

AsaNa is a local information/service guide app (starting with Danao City, Cebu, Philippines) that tells
users what they need, where to go, what it costs, and who to contact for local/government procedures
(e.g. "Barangay Clearance"). Monetization is free + a one-time ₱99 lifetime unlock via Google Play Billing,
with entitlement state verified and owned by the backend — never trusted from the Android client.

Full product/technical spec (personas, milestones, monetization security model, exclusions for MVP, etc.)
was provided by the user in conversation and should be treated as authoritative for product decisions not
covered here.

## Repository layout (monorepo)

```
AsaNa/
├── backend/    Laravel 13 API + Filament admin
└── android/    Kotlin/Compose app
```

## Backend

### Stack

- Laravel 13, PHP 8.5 (composer.json pins `^8.3`; the constraint should be widened deliberately if
  targeting a specific PHP version for deployment)
- PostgreSQL 17, running only in Docker (`backend/docker-compose.yml`) — Laravel itself runs on the host's
  native PHP, not in a container
- Laravel Sanctum installed for future API auth (`routes/api.php`); no register/login/logout endpoints yet
  — deferred to the milestone that needs accounts (favorites/billing), not built ahead of it
- Filament 4 for the admin panel at `/admin`
- Pest for testing (switched from the default PHPUnit-style skeleton per the spec)

### Local setup

```bash
cd backend
docker compose up -d          # starts Postgres only, on 127.0.0.1:5432
composer install
php artisan migrate --seed    # seeds MVP categories + a real Danao City example service
php artisan serve             # http://127.0.0.1:8000
```

`.env` is already configured for the Dockerized Postgres (`DB_DATABASE=asana`, `DB_USERNAME=asana`,
`DB_PASSWORD=secret` — local dev only, not production credentials).

The seeded admin user (from `DatabaseSeeder`) logs into `/admin` with the seeded email and the Laravel
factory default password (`password`) unless changed.

### Common commands

```bash
php artisan migrate:fresh --seed   # rebuild the DB from scratch with seed data
php artisan tinker                 # inspect models/relationships interactively
php artisan make:filament-resource <Model> --generate   # scaffold an admin resource from its schema
vendor/bin/pest                    # run the test suite
vendor/bin/pest --filter=<name>    # run a single test
vendor/bin/pint                    # fix code style (run before committing)
```

### Architecture notes

**Domain model** (`app/Models`): `City` → `Office`/`Service`; `Category` → `Service`; `Office` →
`OfficeHour`; `Service` → `Requirement`, `ServiceFee`. A `Service` belongs to a `City`, `Category`, and
optionally an `Office`, and tracks its own verification state (`status`, `verified_at`, `verified_by`,
`verification_notes`, `source`) directly on the row — this is the core of the "trusted, maintained
content" product differentiator, not an afterthought. `User` → `Entitlement` (Google Play purchase
records) and, separately, `is_admin` gates Filament access (see below).

**Enums, not raw strings.** `App\Enums\ServiceStatus` (draft/review/published/outdated/archived) and
`App\Enums\EntitlementStatus` (active/refunded/revoked/expired) are native PHP backed enums, cast on their
respective models. Extend these rather than adding new string statuses ad hoc.

**Admin access is single-tier for now.** `users.is_admin` is a plain boolean gating `canAccessPanel()`
(`App\Models\User`), not the 3-tier User/Admin/Super Admin split the original spec describes — deferred
until there's a second real admin to justify the split. `is_admin` is deliberately excluded from the
model's mass-assignable `Fillable` list so it can never be set through a normal request; only seeders,
tinker, or a dedicated admin-only action can flip it.

**Filament resource shape.** Only things an admin would manage independently are top-level resources
(`City`, `Category`, `Office`, `Service`, `User`, `Entitlement`). `OfficeHour`, `Requirement`, and
`ServiceFee` are Filament relation managers nested under their parent resource's edit page, not standalone
top-level resources — they only ever make sense in the context of a parent Office/Service, so a global
"all requirements across all services" list would add navigation noise with no real use. This is a
deliberate deviation from the resource list in the original spec doc.

**Public API is publish-gated.** `/api/v1/*` (`app/Http/Controllers/Api`) only ever returns services with
`status = published` — a `Service::scopePublished()` query scope applied in every index/search query, and
an explicit check in `show()` that 404s a draft/review/outdated/archived service even if someone guesses
its slug. This means the seeded Barangay Clearance (left in draft, see above) won't appear in the API until
it's actually published from the admin — that's intentional, not a bug. Route model binding is by `slug`
(`getRouteKeyName()` on `City`/`Service`), not numeric ID. `search` and the `city`/`category` filters on
`index` share one `filteredServices()` query builder rather than duplicating the filter logic per route.

**Entitlements are backend-owned.** The spec's monetization security model (Android sends a Google Play
purchase token → backend verifies with Google → backend writes the `entitlements` row) is not yet
implemented (no billing verification endpoint exists yet) — only the schema and admin CRUD exist so far.
When building it, the verification call to Google Play must happen server-side; never derive `premium =
true` from anything the Android client asserts directly.

**Case-insensitive search: use `whereLike`/`orWhereLike`, never raw `like`.** Postgres's `LIKE` is
case-sensitive; SQLite's (which the test suite runs against) isn't — so a raw `like` scope can pass every
Pest test and still silently fail to match real user input in production. `Service::scopeSearch()` learned
this the hard way (searching "cedula" returned nothing against real data, only caught by testing the
Android app against the live Postgres-backed server, not by the test suite). Laravel's `whereLike()` picks
the correct case-insensitive SQL per driver — use it for any future free-text search.

### Machine-level changes made during setup

These aren't project config — they're one-time changes to this machine's global PHP install, done because
Postgres/Filament required extensions that were present but disabled:

- `C:\php\php.ini`: uncommented `extension=pdo_pgsql`, `extension=pgsql`, `extension=intl`.

If working on a different machine, enable the same extensions before `composer install`/`artisan migrate`
will work.

## Android

### Stack

Kotlin, Jetpack Compose, MVVM + Repository (no separate domain/usecase layer yet — see below), Hilt for DI,
Retrofit + OkHttp + kotlinx.serialization for networking, Navigation Compose. Application ID `ph.asana.app`,
minSdk 26, compileSdk/targetSdk 35. Room (offline caching) and Google Play Billing are not built yet —
they're Milestones 4 and 6, not built ahead of them.

### Local setup

```bash
cd android
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # or Android Studio's bundled JDK
./gradlew assembleDebug
```

`local.properties` (gitignored) must contain `sdk.dir=<path to Android SDK>`. The app talks to
`http://10.0.2.2:8000/api/v1/` by default (the emulator's alias for the host machine's localhost, where
`php artisan serve` runs) — override per-build with `-PASANA_API_BASE_URL=http://<host>:8000/api/v1/` when
testing on a physical device on the same network. Cleartext HTTP is allowed only to `10.0.2.2`
(`res/xml/network_security_config.xml`) — production must be HTTPS against the real domain, don't widen this.

### Architecture notes

**No domain layer yet, on purpose.** The spec's suggested structure has a `domain/model` + `domain/usecase`
layer between `data` and `feature`. With a single data source (the REST API, no Room yet), that mapping
layer has nothing real to do — repositories return the network DTOs (`network/model/Dtos.kt`) directly as
UI state. Revisit this once Room is added in Milestone 4 and there are two sources to reconcile; don't add
it preemptively.

**Screens are MVVM: Compose UI + Hilt `ViewModel` + shared `UiState<T>`** (`ui/UiState.kt` — a small
Loading/Success/Error sealed interface reused by all four screens, since they all needed the identical
shape from day one). `AsaNaRepository` wraps every API call in `runCatching`, so ViewModels never touch
Retrofit exceptions directly.

**Emergency contacts are hardcoded, and deliberately incomplete.** The Home screen shows only the
Philippines' National Emergency Hotline (911) — a real, verifiable, nationwide number. Local Danao City
numbers (police, fire, DRRMO, barangay) are NOT in there, because fabricating plausible-looking emergency
phone numbers would be actively dangerous if wrong. There's no backend table for these either (not in the
spec's ERD) — add one (and real, verified numbers) before shipping them.

**Retrofit's kotlinx.serialization converter.** The `com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter`
artifact's Kotlin-visible API is the extension function `Json.asConverterFactory(mediaType)`, imported from
`com.jakewharton.retrofit2.converter.kotlinx.serialization` — not `retrofit2.converter.kotlinx.serialization`
as most examples imply, and not a `KotlinSerializationConverterFactory.create(...)` static call either (that
method exists in the compiled bytecode but is the JVM-facing implementation of the same extension function,
not the intended entry point). Easy to get wrong; `di/NetworkModule.kt` has the correct usage.

## Engineering approach for this project

The user is beginner-to-intermediate (PHP/Laravel/REST background, new to Android) building toward a real
Play Store release, solo. Act as a senior engineer/architect would, not as a code generator:

- Prevent overengineering and scope creep — the spec has an explicit MVP exclusion list (no AI chatbot,
  no reviews/UGC, no appointment booking, no microservices, no Elasticsearch, etc.) and a milestone order.
  Don't build ahead of the current milestone without discussing it first.
- Explain non-obvious architectural decisions (what problem it solves, why, what the alternative was) —
  don't just hand over code.
- Flag security and scalability issues proactively, especially around the billing/entitlement flow and
  anything trusting client-supplied data.
- Prioritize shipping the MVP over completeness; a small working slice beats a large unfinished one.

## Git workflow

- Commit messages follow the 50/72 rule: subject line ≤50 chars, body wrapped at 72 chars. Keep them
  concise — a short subject plus a couple of sentences of "why", not an exhaustive changelog.
- PR titles and descriptions are written in plain language describing what changed — no conventional-commit
  prefixes like `fix:`/`feat:`.
- Group commits by logical concern (e.g. "scaffold the app" separate from "add domain schema" separate from
  "add admin resources") rather than one giant commit or one commit per file.

## Code quality rules

- No over-engineering. Solve the problem in front of you, not the one you imagine might show up later.
  No speculative config options, extension points, or "just in case" parameters — add them when a real
  second case demands them, not before.
- Naming must be simple and literal. Say what the thing is or does (`getUser`, `retryCount`, `isValid`) —
  no cleverness, no abbreviations that aren't standard, no vague names (`data`, `helper`, `manager`,
  `util`) when a specific name is available. Consistent naming beats "better" naming — match whatever
  convention is already established in the codebase.
- Structure must stay flat and obvious. Don't add a new folder, module, or layer of indirection until the
  current one is genuinely too crowded to navigate. A function that's called once doesn't need to be
  "reusable"; a module with one implementation doesn't need an interface/abstract base in front of it.
- Logic must be linear and readable top to bottom. Prefer early returns over nested conditionals, prefer
  explicit branches over clever one-liners, prefer a few extra lines that are obvious over a compact
  version that requires re-reading. If a reviewer would need a comment to understand a line, rewrite the
  line instead of adding the comment.
- Every abstraction (function, class, module, config layer) must earn its place by removing real, existing
  duplication or complexity — not by anticipating hypothetical future needs. When in doubt, inline it; it's
  cheaper to extract later than to unwind a wrong abstraction.
- Delete dead code and unused parameters immediately rather than leaving them "for later" or commented out.
