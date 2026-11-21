# Product catalogue challenge

## Backend

The backend is a small Node.js and TypeScript HTTP API. It uses Node's built-in HTTP server and `fetch`, with Nodemailer for SMTP delivery; there is no HTTP framework. The Fake Store API is accessed only by the backend through `backend/src/products.ts`, so the Android app talks to this API rather than to Fake Store directly.

### Run with Docker (recommended)

From the repository root, run:

```sh
docker compose up --build
```

The API listens on port `3000`. Check it with `http://localhost:3000/health`. To change the host port or product source URL, set `PORT` or `FAKE_STORE_API_URL` in the shell before starting Compose.
Compose also starts Mailpit as a free local SMTP inbox. Open `http://localhost:8025` to see enquiry emails delivered to `sales@example.test`. Set `SALES_INBOX` and `MAIL_FROM` in a root `.env` file to change the local addresses; messages remain inside Mailpit and are not sent to an external service.

### Run without Docker

Requires Node.js 22 or newer.

```sh
cd backend
npm install
npm run dev
```

Export values from `backend/.env.example` in your shell if you want to change configuration. Node does not load `.env` files automatically. To configure Docker Compose, copy the root `.env.example` to `.env`; Compose reads that file automatically.

### Backend API checks

The integration checks use Node's built-in test runner and a local fake product source. From the backend directory:

```sh
npm test
```

### Android connection

Configure the Android app's API base URL per build/run configuration:

- **Android Emulator:** `http://10.0.2.2:3000` reaches port 3000 on the development computer.
- **Physical Android device:** use `http://<computer-LAN-IP>:3000`, for example `http://192.168.1.20:3000`. Put the phone and computer on the same Wi-Fi and allow connections to port 3000 in the computer firewall.

The backend binds to `0.0.0.0`, which allows the physical-device option. For local HTTP on Android, cleartext traffic is enabled only in the debug manifest; use HTTPS for a deployed backend. The app reads its URL from `BuildConfig.API_BASE_URL`, with an emulator-friendly default. Run these commands from the repository root. Start the backend in one terminal:

```sh
docker compose up --build
```

Then install the app from a second terminal. For an Android Emulator:

```sh
./gradlew installDebug -PAPI_BASE_URL=http://10.0.2.2:3000
```

For a physical Android device connected with USB debugging, run:

```sh
LAN_IP="$(ipconfig getifaddr en0)"
echo "Using computer IP: ${LAN_IP}"
./gradlew installDebug -PAPI_BASE_URL="http://${LAN_IP}:3000"
```

Keep the phone and computer on the same Wi-Fi network, and allow incoming connections to port `3000` in the computer firewall. If your Mac's Wi-Fi interface is not `en0`, find it with `networksetup -listallhardwareports` and replace `en0` in the command with the listed device name.

The Gradle property can also be set through an `API_BASE_URL` environment variable. To build without installing to a device or emulator, use:

```sh
./gradlew assembleDebug
```

Start the API before launching the app.

### Android app

Requires JDK 17 and Android SDK platform 36. With an emulator running and the backend available on port 3000:

```sh
./gradlew installDebug
```

The Android app includes the featured-products home screen, category browsing, submitted search, product details, and an enquiry form. It follows presentation → domain → data boundaries: the ViewModel calls use cases, use cases use a repository interface, and the data repository loads JSON from the backend API. Enquiry submissions return the backend's thank-you response after the email has been accepted.

### Android screen tests

With an Android Emulator running, run the Compose screen tests from the repository root:

```sh
./gradlew connectedDebugAndroidTest
```

The tests cover featured products and category navigation, category-load retry, search submission and empty results, product details and its error state, and enquiry validation, submission, errors, and thank-you feedback.

### API routes

- `GET /health`
- `GET /api/products` and `GET /api/products?q=term`
- `GET /api/products/featured` (demo featured IDs 1–5)
- `GET /api/products/:id`
- `GET /api/categories`
- `GET /api/categories/:category/products` (sorted by rating, highest first)
- `POST /api/enquiries` with JSON `{ "productId": 1, "name": "Ada", "email": "ada@example.com", "message": "Please contact me" }`

Enquiries are validated and sent via SMTP to Mailpit in the Docker setup. Without SMTP configured, the backend captures the payload in its log for quick local development. No paid service or account is required.
