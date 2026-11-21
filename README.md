# Product catalogue challenge

## Backend

The backend is a small Node.js and TypeScript HTTP API. It uses Node's built-in HTTP server and `fetch`; there is no web framework or runtime dependency. The Fake Store API is accessed only by the backend through `backend/src/products.ts`, so the Android app talks to this API rather than to Fake Store directly.

### Run with Docker (recommended)

From the repository root, run:

```sh
docker compose up --build
```

The API listens on port `3000`. Check it with `http://localhost:3000/health`. To change the host port or product source URL, set `PORT` or `FAKE_STORE_API_URL` in the shell before starting Compose.

### Run without Docker

Requires Node.js 22 or newer.

```sh
cd backend
npm install
npm run dev
```

Copy `backend/.env.example` to `backend/.env` if you want to change configuration. Node does not load `.env` files automatically; export the variables in your shell or use Docker Compose, which reads a root `.env` file.

### Android connection

Configure the Android app's API base URL per build/run configuration:

- **Android Emulator:** `http://10.0.2.2:3000` reaches port 3000 on the development computer.
- **Physical Android device:** use `http://<computer-LAN-IP>:3000`, for example `http://192.168.1.20:3000`. Put the phone and computer on the same Wi-Fi and allow connections to port 3000 in the computer firewall.

The backend binds to `0.0.0.0`, which allows the physical-device option. For local HTTP on Android, cleartext traffic is enabled only in the debug manifest; use HTTPS for a deployed backend. The app reads its URL from `BuildConfig.API_BASE_URL`, defaulting to the emulator URL. Override it without editing app source:

```sh
# Physical device; replace the address with your computer's LAN IP
./gradlew installDebug -PAPI_BASE_URL=http://192.168.1.20:3000
```

Requires JDK 17 and Android SDK platform 36. From the repository root run `./gradlew installDebug` with an emulator running. The app displays the five featured products and lets customers browse category products with the highest rated first.

### API routes

- `GET /health`
- `GET /api/products` and `GET /api/products?q=term`
- `GET /api/products/featured` (demo featured IDs 1–5)
- `GET /api/products/:id`
- `GET /api/categories`
- `GET /api/categories/:category/products` (sorted by rating, highest first)
- `POST /api/enquiries` with JSON `{ "productId": 1, "name": "Ada", "email": "ada@example.com", "message": "Please contact me" }`

Enquiries are validated and printed to the backend log as a local email-capture substitute. No email service or account is required for the proof of concept. A real sales inbox integration can replace that handler later.
