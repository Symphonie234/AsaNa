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

A root-level `Makefile` wraps the commands above (and the Docker/Android equivalents) for
convenience — run `make help` from the repo root to see the full list (`make dev`, `make test`,
`make android-build`, etc.). It's a thin wrapper only; nothing in it is required, and the raw
commands above always work too.

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
its slug. The seeded Barangay Clearance was published from the admin during testing, so it's now visible —
new seed content still starts in draft and needs the same manual publish step. Route model binding is by
`slug` (`getRouteKeyName()` on `City`/`Service`), not numeric ID. `search` and the `city`/`category` filters
on `index` share one `filteredServices()` query builder rather than duplicating the filter logic per route.

**Auth is Sanctum personal access tokens, not SPA cookie auth.** `/api/v1/auth/register` and `/auth/login`
are public (rate-limited to 5/min — see spec's brute-force concern), return a plain-text token the Android
client stores and sends as `Authorization: Bearer <token>`. `/auth/logout`, `/me`, and `/me/favorites/*` are
behind `auth:sanctum`. Browsing/search/service-detail stay fully open with no account required — an account
is only needed to save a favorite — matching the spec's "simple, practical" principle over gating the core
value prop behind signup.

**Favorites is a plain pivot table, not in the original ERD.** `favorites` (`user_id`, `service_id`, unique
together) backs `User::favoriteServices(): BelongsToMany`. The spec's ERD (section 19) never modelled this —
only the narrative product description (section 9) did — so it was added following the same conventions as
the rest of the schema, not literally copied from a spec table.

**Google Play purchase verification is hand-rolled, not `google/apiclient`.** `app/Services/Billing/` signs
a service-account JWT with `firebase/php-jwt`, exchanges it for an OAuth2 access token, then calls the one
Android Publisher REST endpoint we actually need (`purchases.products.get`) via Laravel's `Http` facade —
`google/apiclient` generates bindings for Google's entire API surface to reach that same one endpoint, which
is a lot of weight for a ~30-line OAuth2 flow. `GooglePlayVerifier` is an interface so tests bind a fake
implementation instead of needing real Google credentials (`tests/Feature/Api/BillingApiTest.php`) — the
real `GooglePlayApiVerifier` is what runs in production once `GOOGLE_PLAY_SERVICE_ACCOUNT_PATH` is set.
**This can't be end-to-end tested yet** — it requires a Google Play Console developer account (one-time $25
registration, only the project owner can do this), the app registered there with the `asanaph.lifetime`
in-app product created, and a Service Account JSON key. Until then, `/billing/google-play/verify` correctly
returns a clean 503 ("Billing verification is not configured") rather than crashing — verified live.

**Refund/revocation handling is manual admin action for now, not a Google RTDN webhook.** Real-time
Developer Notifications (Google's push mechanism for refunds/cancellations) need a Google Cloud Pub/Sub
topic wired to Play Console — another piece of external account setup that doesn't exist yet. Until it's
built, the Filament `EntitlementResource` (already in the admin from Milestone 2) lets an admin manually
flip `status` to `refunded`/`revoked` when Google notifies them by email. Automating that via RTDN is a
reasonable Milestone 7 (production hardening) follow-up once Play Console exists.

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
Retrofit + OkHttp + kotlinx.serialization for networking, Room for offline caching, EncryptedSharedPreferences
for the auth token, Play Billing Library (`billing-ktx`) for the lifetime purchase, Navigation Compose.
Application ID `ph.asana.app`, minSdk 26, compileSdk/targetSdk 35.

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

### Turning on real Google Play billing

The code is ready; these are the external, account-holder-only steps nobody but the project owner can do:

1. Register a Google Play Console developer account (one-time $25 fee).
2. Create the app in Play Console with application ID `ph.asana.app`, upload at least one build to an
   internal testing track (real purchases — even test ones — require this; a locally-built debug APK alone
   can't complete a purchase, Play Billing needs the app to be known to Play Console).
3. Under Monetization → Products → In-app products, create a **managed product** with ID `asanaph.lifetime`
   (must match exactly — see `BillingRepository.LIFETIME_PRODUCT_ID` and the spec's product ID).
4. Add license testers (Setup → License testing) so test purchases don't charge a real card.
5. In Google Cloud Console, create a Service Account linked to the Play Console account, grant it access
   under Play Console → Users and permissions, enable the Android Publisher API, and download its JSON key.
6. On the backend, set `GOOGLE_PLAY_SERVICE_ACCOUNT_PATH` to that JSON file's path (and
   `GOOGLE_PLAY_PACKAGE_NAME` if it's ever not `ph.asana.app`) — `/billing/google-play/verify` picks it up
   automatically, no code change needed.

### Architecture notes

**No domain layer yet, on purpose.** The spec's suggested structure has a `domain/model` + `domain/usecase`
layer between `data` and `feature`. Even now that there are two data sources (network + Room, see below),
they share the exact same model — repositories return the network DTOs (`network/model/Dtos.kt`) directly
as UI state, cached and read back as the same DTOs. There's still no real mapping work for a domain layer
to do. Revisit once a second *shape* of data source shows up (e.g. a local-only favorites list), not before.

**Offline caching is a JSON cache-aside, not a mirrored schema.** `data/local/` has one Room table
(`CachedResponse`: a `key` string primary key, a `json` blob, `cachedAt`) instead of Room entities that
mirror the backend's `cities`/`services`/`requirements`/etc. tables. `AsaNaRepository.networkFirst()` tries
the network, writes the successful response's JSON under a key derived from the request (e.g.
`"service:barangay-clearance"`, `"services:city=danao-city&category=null"`) on success, and on an `IOException`
(no connection — not a 404 or a bad response, which are real errors and still fail) falls back to whatever
JSON is cached under that key, decoded back into the same DTO. This means content becomes available offline
once it's been viewed online at least once (e.g. looked up at home on wifi, still there later at the office
with no signal) — it is NOT a "download everything for offline use" cache; a service never opened while
online has nothing to fall back to and will show an error offline. That's the intentional scope of Milestone
4's goal ("important service information remains available offline"), not a gap to fix reflexively.
Mirroring the full relational schema in Room would only be worth the extra entity/mapping code if the app
needed real offline queries or writes across that data — it doesn't yet.

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

**Favorites is a shared `StateFlow` in `FavoriteRepository`, not per-screen fetches.** Compose Navigation
preserves a bottom-nav tab's state when you switch away and back (normally what you want), so a screen that
only loaded favorites in its own `init{}` kept showing a stale list after Service Detail added one
elsewhere — the save genuinely succeeded on the backend, the Favorites tab just never found out. Every
mutation (`refresh`/`addFavorite`/`removeFavorite`) updates the same `favorites: StateFlow<List<ServiceDto>>`,
and any screen collecting it (currently `FavoritesScreen`) reflects changes made by any other screen
immediately. `AuthRepository.logout()` (and the 401 auto-logout path) calls `favoriteRepository.clear()` so
a new sign-in doesn't briefly show the previous user's list.

**Auth failures surface the backend's actual message, not a hardcoded string.** `network/ApiException.kt`
parses Laravel's `{"message": "..."}` validation body out of an `HttpException` (falling back to a generic
string only when there isn't one, e.g. a real connectivity failure) — a real "The email has already been
taken." beats a generic "check your details" every time; the latter is what turned an unrelated seeded
admin account into a support back-and-forth before the actual cause was visible in Logcat.

**Auth session state lives in `AuthRepository.currentUser` (a `StateFlow<UserDto?>`), not a boolean flag.**
`auth/TokenStore.kt` holds the Sanctum bearer token in `EncryptedSharedPreferences` (it's equivalent to a
password, not a UI preference) and `auth/AuthInterceptor.kt` attaches it to every request when present.
`FavoritesViewModel` and other screens collect `currentUser` reactively rather than checking `isSignedIn`
once, so signing in or out anywhere (e.g. the Settings tab) correctly updates the Favorites tab without
that screen needing to know it happened. `MainActivity` hydrates `currentUser` from a stored token on cold
start via `refreshCurrentUser()` — it only clears the token on an actual 401, never on a network error,
since "can't reach the server right now" isn't the same claim as "this token is invalid."

**Bottom nav (Home/Favorites/Settings) vs. pushed screens (Search/Category/Service Detail).** Only the
three destinations that make sense to jump between directly are tabs; the rest stay reachable by navigating
from Home, matching the original screen flow. The "Save" button on Service Detail is a one-way action (adds
to favorites, prompts sign-in first if needed) with no in-place "unsave" toggle — that matches the spec's
own mockup, which shows a single `[ Save ]` button; removing a favorite happens from the Favorites tab's own
delete icon, not from the detail screen.

**`BillingRepository` owns its own `CoroutineScope`, unlike every other repository.** Auth/Favorite/content
repositories are plain suspend-function + `StateFlow` holders that borrow whatever `viewModelScope` calls
them — that works because every state change they make is triggered by an explicit call from a ViewModel.
Billing is different: `PurchasesUpdatedListener.onPurchasesUpdated` is a plain callback invoked directly by
the Play Billing library itself (not by anything our code calls), and the `BillingClient` connection needs
to live for the whole app process, not one screen's lifecycle. `MainActivity` field-injects it (same pattern
as `AuthRepository`) purely to force Hilt to construct the singleton — and its `connect()` — at app start.

**`isPremium` comes from our backend, never from what Play Billing reports locally.** `BillingRepository`
calls `GET /billing/entitlements` to determine premium status and re-checks on every sign-in; a successful
local purchase only flips `isPremium` after our backend has verified the purchase token with Google and
recorded an entitlement. This is the same "never trust the client" rule as everywhere else in the app,
applied to the one place a fake client-side flag would be most tempting to just trust.

**Real purchases can't be tested on this emulator.** The AVD's system image doesn't include Google Play
Store services, so `BillingClient` can't connect at all here (`"In-app billing API version 3 is not
supported on this device"`, logged as a warning, not a crash). Confirmed the app degrades gracefully — no
crash, "Unlock Lifetime Access" falls back to generic text with no live price, Restore Purchases doesn't
crash either — but the actual purchase flow needs either a real device signed into a Google account, or an
emulator image with Play Store, *and* the app registered in Play Console with the product created there.

### Emulator testing gotchas

These cost real time this session — worth not re-learning them:

- **Get tap coordinates from `adb shell uiautomator dump`, not by eyeballing a screenshot.** Screenshots
  returned to Claude get rescaled for display, and manually estimating "where a button looks like it is"
  in the rescaled image is consistently off by enough pixels to miss real click targets — this produced a
  long, wrong detour into suspecting the emulator/Compose touch pipeline was broken when the actual problem
  was tap coordinates landing in the gaps between elements. `uiautomator dump /sdcard/dump.xml` (then `adb
  pull`) gives exact `bounds="[x1,y1][x2,y2]"` for every element — compute the center from that, every time.
- **The Pixel_7 AVD can leave a genuinely stuck instance behind that still answers `adb devices` and renders
  frames, but stops delivering touch input**, while hardware key events (`KEYCODE_HOME`) still work. The
  giveaway is `adb shell input tap` doing nothing anywhere in the app, including on elements that worked in
  a previous session with unchanged code. Killing it from `emulator.exe`/`adb emu kill` is not reliable —
  the actual VM process is named `qemu-system-x86_64-headless` (note the suffix), not `qemu-system-x86_64`,
  so a `Stop-Process -Name "qemu-system-x86_64"` misses it silently. Use
  `Get-Process | Where-Object { $_.ProcessName -like "*qemu*" -or $_.ProcessName -like "*emulator*" } |
  Stop-Process -Force`, then relaunch with `-no-snapshot` to force a genuine cold boot rather than resuming
  the stuck saved state.

## Production hardening (Milestone 7)

Spec section 35 asks for: Crashlytics, logging, rate limits, backups, monitoring, CI/CD, and a
security review. Split into what's buildable now versus what needs real external infrastructure
that doesn't exist yet:

### Done now

- **CI/CD** — `.github/workflows/backend.yml` and `.github/workflows/android.yml`. Backend runs
  Pint (style) and Pest (tests, against in-memory SQLite, same as local — no services/secrets
  needed). Android runs `lintDebug`, `testDebugUnitTest`, and `assembleDebug`. Both are
  path-filtered so an Android-only change doesn't run the backend job and vice versa. Neither
  workflow needs secrets: there's no `google-services.json` dependency yet (Milestone 6's billing
  code talks to Google Play's REST API directly, not a Firebase SDK), so nothing breaks in CI.
- **Rate limiting** — audited and found a real gap: `bootstrap/app.php` used `withRouting(api:
  ...)` but never called `->throttleApi()`, and no `api` limiter was registered, so **every**
  endpoint except `auth/register`/`auth/login` (which already had explicit `throttle:5,1`) had no
  rate limiting at all. Fixed by adding `$middleware->throttleApi()` in `bootstrap/app.php` plus an
  `api` limiter in `AppServiceProvider::boot()` (60/min, keyed by user ID when authenticated,
  otherwise IP) as the default floor for every `v1` route. `billing/google-play/verify` gets a
  tighter `throttle:10,1` on top of that, since it triggers an outbound call to Google's API per
  request and is the one endpoint where abuse has an external cost. Covered by re-running the full
  Pest suite after the change (all 30 tests still pass) rather than just eyeballing it — the fix
  itself broke tests once (`MissingRateLimiterException`) until the `api` limiter was registered,
  which is exactly the kind of thing that's easy to get half-right.
- **Security review** — a real pass, not just a checkbox, over what's built so far:
  - Mass assignment: every model uses `$fillable` (none use `$guarded = []`), and
    `AuthController::register` writes `$request->validated()`, not raw input. Safe.
  - Response shape: every API response goes through an explicit-allowlist `JsonResource` — no
    resource serializes a model directly, so there's no path for `password` or other hidden
    columns to leak into JSON even by accident.
  - CORS: no `config/cors.php` and none needed — this API is consumed only by the Android app via
    Bearer tokens, never by a browser, so there's no cross-origin cookie/session surface to worry
    about.
  - Sanctum tokens don't expire (`'expiration' => null`), but `logout()` explicitly revokes the
    current token (`currentAccessToken()->delete()`). This is a deliberate accepted tradeoff for a
    single-device mobile app, not an oversight — revisit only if a "sign out of all devices" or
    remote-wipe feature is ever needed.
  - Android's `network_security_config.xml` already scopes the cleartext-HTTP exception to
    `10.0.2.2` only (the emulator's loopback to the host), with a comment warning not to widen it
    for release builds. Nothing to change there.
- **Logging** — Laravel's default `stack`/`single` channel is fine as-is for this stage; nothing
  bespoke needed until there's a real server to ship logs from.

### Deferred — needs real external setup first

These can't be meaningfully built against nothing; doing so now would mean throwaway config or,
worse, a broken build. Come back to each once the prerequisite exists:

- **Crashlytics** — needs a real Firebase project and a real `google-services.json`. Adding the
  Crashlytics Gradle plugin without a real config file breaks the Android build outright, so this
  waits until a Firebase project is created (same "register the external account first" shape as
  the Play Console checklist above).
- **Backups** — needs a real production database to back up. Nothing to configure against a local
  Docker Postgres container; revisit once there's a real hosting target.
- **Monitoring** (uptime/error tracking/APM) — same reasoning as backups: needs a real deployed
  environment to monitor. Revisit at deploy time.

## Beta feedback (Milestone 8)

The spec's Milestone 8 is "recruit 10–20 real Danao residents, hand them a task with zero
explanation, and observe where they get confused" — that's a real-world usability test, not
something buildable in code. What *was* built is the tooling to support running it:

- **`POST /api/v1/feedback`** — public (no login required, so a tester who isn't a registered
  user can still report something), rate-limited (`throttle:10,1`) like every other public
  write endpoint. Captures the message plus `context` (which screen), `app_version`, and
  `device_info` for triage — and the user id too, if the request happens to carry a valid
  Sanctum token, but it's never required. Feedback is intentionally **not modeled as a public
  resource** — no `FeedbackResource` API response, no way to list other people's feedback from
  the client — it's a one-way "send this to the admin" channel, not user-generated content
  visible in the app (the spec's MVP exclusions rule out UGC/reviews for a reason).
- **`/admin/feedback`** (Filament) — read-mostly by design: there's no "create" page, since a
  fabricated feedback record serves no purpose an admin would need. The submitted fields
  (`message`, `context`, `app_version`, `device_info`) are shown disabled on the edit page —
  editing what a real tester actually reported would defeat the point — and the only thing an
  admin can change is toggling `is_resolved` once they've triaged it. Default-sorted newest
  first, with a filter for unresolved-only.
- **Android: Settings → "Report a problem"** — an `AlertDialog` (not a full screen/nav route;
  a one-off action doesn't need back-stack complexity), wired through a small
  `FeedbackRepository`/`FeedbackViewModel` pair following the same `Result`-returning,
  `sealed interface` event pattern used everywhere else in the app (see `AuthViewModel` for
  the original shape this follows). Verified live end-to-end on the emulator — submitted
  through the actual Compose dialog, confirmed the row landed in Postgres with the right user,
  device info (`Google sdk_gphone16k_x86_64, Android 17`), and app version attached.

**Running the actual beta test is documented in
[docs/milestone-8-beta-testing.md](docs/milestone-8-beta-testing.md)** — the task script,
what to observe, and a log template. It's still blocked on real people and, for proper
distribution, the same Play Console setup deferred in Milestone 6 — for now the APK gets
shared with testers directly (fine at 10–20 people, not a long-term distribution plan).

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

### Branching (per milestone)

Starting from Milestone 8 onward (Milestones 0-7 were built directly on `main`, before this rule
existed), every milestone gets its own branch instead of committing straight to `main`:

```
main → develop → AsaNa/{milestone number}-{short-kebab-title}
```

- `develop` branches off `main` and is the shared integration branch — it doesn't get deleted between
  milestones.
- Each milestone branches off `develop`, named `AsaNa/{number}-{title}` (e.g. `AsaNa/8-launch-prep`).
- Work happens on the milestone branch, merges back into `develop` when the milestone is done and
  tested, and `develop` merges into `main` when it's ready to ship.
- Before starting a new milestone's work, make sure `develop` exists and is branched from `main` first
  if it doesn't already.

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
