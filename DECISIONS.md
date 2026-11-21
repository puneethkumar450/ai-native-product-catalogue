# Decisions and assumptions

## Assumptions

- Fake Store API does not provide a reliable editorial "top rated" flag. The home page therefore features product IDs 1–5 as a documented demo choice.
- Category results include every product in that category, sorted by rating from highest to lowest. This puts the top rated items first while still showing the rest.
- Search is a case-insensitive substring match over product title, description, and category. It runs when the user submits the search form.
- An enquiry requires a product ID, customer name, and valid email. The message is optional and limited to 2,000 characters by the backend.
- In the Docker setup, the email goes to a local Mailpit inbox at `http://localhost:8025`; it is not sent to a real external sales address. This provides a free, reviewable inbox for the proof of concept.

## Simplifications

- Product metadata comes from Fake Store API through a backend adapter. There is no product database or cache in this proof of concept.
- The email transport is SMTP, configured to Mailpit locally. A real SMTP provider and verified sales address are deployment configuration work.
- The Android debug build allows cleartext HTTP for local development. The release manifest does not enable cleartext; a deployed API should use HTTPS.
- The app implements the required catalogue and enquiry paths without accounts, order flows, analytics, or deployment infrastructure.

## Growth path

- The Android app separates Compose presentation, ViewModels/use cases, and repository interfaces from the HTTP data source.
- The backend keeps the external product source in `backend/src/products.ts` and enquiry delivery in `backend/src/mailer.ts`. These are the replacement points for a future catalogue source and mail provider.
- The public API is the contract between Android and the backend, so the product source can change without making the app call it directly.

## With another day

- Use a persistent catalogue source and store enquiry submissions with delivery status.
- Configure a real email provider, secrets, sender identity, retries, and operational logging for deployment.
- Add Android UI and ViewModel tests plus deeper email delivery checks.
- Improve accessibility and visual polish based on the entrepreneur's design direction.
