# AI usage notes

## Tools and models

- OpenAI Codex with GPT-6 for planning, implementation, documentation, and review.
- Shell tools for source changes, Gradle/npm builds, and the Node built-in API test runner.
- No external coding agents were used.

## Instructions and plan

- No `AGENTS.md`, `CLAUDE.md`, or editor-specific instruction files were added.
- The plan was to build the backend API boundary first, then the Android app's data/domain/presentation path, then verify the required catalogue and enquiry flows.

## Prompts and decisions that shaped the implementation

- The client was fixed to Android Kotlin with Jetpack Compose, MVVM, and Clean Architecture.
- The backend was requested first, kept small, and built without an HTTP framework.
- The Android API URL was required to be configurable for emulator and physical-device use.
- Work was constrained to the PDF's acceptance and submission criteria, with no paid services or unnecessary libraries.
- After noticing that enquiry submission only logged data, the implementation added Mailpit as a local SMTP inbox to satisfy the email acceptance path without requiring an account.

## AI mistakes caught during review

- The first Android dependency set included Compose and lifecycle artifacts requiring API 37 while the project used Android Gradle Plugin 8.13.2 and API 36. The Gradle build caught this, and the dependencies were pinned to a compatible set.
- The first Docker runtime stage omitted the newly added SMTP library. Reviewing the Dockerfile caught this; the runtime stage now installs production dependencies from the lockfile.
- A product-detail screen initially failed Kotlin smart-cast compilation when reading product state from Compose collection. The compiler caught it; the selected product is now held in a local immutable value before branching.

Every generated change was reviewed and the backend build, API checks, and Android debug build were run locally.
