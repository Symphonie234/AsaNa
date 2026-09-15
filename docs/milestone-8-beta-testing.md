# Milestone 8 — Beta Testing

This is a runbook for the one part of Milestone 8 that has to happen with real people —
nothing here can be automated or built in advance. See [CLAUDE.md](../CLAUDE.md) for what
was built to support it (the in-app "Report a problem" feature).

## Goal

Per the spec's success criteria: the first goal isn't 10,000 users, it's **10 real people
who can use AsaNa successfully without your help.** This test measures exactly that.

## Who

**10–20 people**, preferably Danao City residents (they'll recognize the offices and
procedures, which is the whole point). A mix of ages and how comfortable they are with
apps in general is more useful than 20 similar people.

## Getting the app to them

The Play Console isn't set up yet (see CLAUDE.md's "Turning on real Google Play billing"
checklist), so there's no Play Store internal testing track to distribute through. For
now: build a debug or release APK and share it directly (a link, or install it on their
phone yourself). This is fine for 10–20 people — it only becomes a problem at real scale.

## Running a session

1. **Give them one task, out loud, and stop talking.**

   > "You need a [Barangay Clearance / Business Permit / Birth Certificate]. Use this app
   > and figure out what you need to do."

   Pick a service that's actually seeded in the database. Don't explain the app, don't
   point at buttons, don't clarify the task if they ask — that's the point of the test.

2. **Watch, don't help.** Let them struggle. Resist the urge to jump in when they're
   about to tap the wrong thing — that hesitation is the data.

3. **Take notes as they go**, not from memory afterward. Use the log template below.

4. **If they hit a real bug or point of confusion**, that's what the in-app "Report a
   problem" button (Settings → Report a problem) is for — encourage them to use it if
   something breaks, even though you're also watching. It gets you a written record and
   confirms the feature itself works under real use.

5. **Afterward, ask 2–3 questions, not a survey:**
   - "What was confusing, if anything?"
   - "Was there a point where you weren't sure what to do next?"
   - "Would you actually use this the next time you needed [the task]?"

## What to watch for specifically

- Do they use Search or browse Categories first?
- Do they find the Requirements, Fee, and Office hours on the service detail screen, or
  do they miss them?
- Do they know to tap Save/favorite it for later?
- Do they successfully get to "where do I go and when" — the spec's core value
  proposition — without asking you?
- Where do they pause, backtrack, or mis-tap?

## Log template

One row per tester:

| Tester # | Task given | Completed without help? | Where they got stuck | Notable quote |
|---|---|---|---|---|
| 1 | | Y / N | | |
| 2 | | Y / N | | |

## After all sessions

1. Check the feedback inbox at `/admin/feedback` for anything testers reported mid-session.
2. Look across your log for **repeated** stuck points — one person struggling once is
   noise, three people stuck at the same screen is signal.
3. Turn the repeated ones into concrete fixes before Milestone 9 (Play Store release).
   Don't fix one-off confusion from a single tester; the spec's own principle applies
   here too — prioritize shipping over chasing every edge case.
