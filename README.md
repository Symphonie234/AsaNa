# AsaNa

**AsaNa** is a mobile app that helps people in Danao City, Cebu find local government
services without the guesswork. Need a Barangay Clearance, a business permit, or a
health certificate? AsaNa tells you what it is, where to go, what it costs, what to
bring, and who to contact — all in one place, in plain language.

The name comes from the Cebuano word *"asa"* ("where"), as in *"asa ni?"* — "where is
this?" — which is exactly the question the app answers.

## What you can do with it

- **Browse services** by category — permits, clearances, certificates, health, business,
  and more.
- **Search** for what you need by name, without knowing which office handles it.
- **See the details that matter**: requirements, fees, processing time, office location,
  and contact information.
- **Save your favorites** so the services you use often are one tap away.
- **Access everything offline-friendly** once you've viewed it, so a weak signal at a
  government office doesn't leave you stuck.

The app is **free to use**. A one-time payment unlocks the full set of services for
good — no subscriptions, no recurring fees.

## Who it's for

Anyone who needs to get something done at a local government office in Danao City and
would rather not spend a trip finding out they went to the wrong window, brought the
wrong requirement, or showed up on the wrong day.

## Project status

AsaNa is under active development. The backend (the system that stores and serves all
the service information) and the Android app are both being built and tested step by
step, milestone by milestone, with real devices and real data along the way.

## For developers

This is a monorepo with two parts:

```
AsaNa/
├── backend/    Laravel API + admin panel, backed by PostgreSQL
└── android/    Kotlin/Jetpack Compose Android app
```

Day-to-day commands are wrapped in a `Makefile` so you don't need to remember the long
versions. From the project root:

```bash
make help      # list every available command
make up        # start the database (Docker)
make dev       # start the database, then migrate and seed it
make serve     # run the backend API locally
make test      # run the backend test suite
make pint      # auto-fix backend code style

make android-build     # build the Android debug APK
make android-install   # build and install it on a connected device/emulator
make android-test      # run Android unit tests
```

For the full technical setup, architecture notes, and engineering decisions behind the
project, see [CLAUDE.md](CLAUDE.md).
