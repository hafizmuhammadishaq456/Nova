# Nova AI Assistant - Secure Backend

This is the official Node.js / Express backend service for the **Nova Android AI Assistant**.

## Security Design
- The OpenAI API key is **never** included or exposed inside the Android client APK.
- The Android app communicates with this backend over HTTPS / HTTP via `POST /api/chat`.
- The backend loads `OPENAI_API_KEY` strictly from environment variables.

## Getting Started

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Set up environment variables:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and set your real OpenAI API key:
   ```env
   OPENAI_API_KEY=sk-proj-...
   PORT=3000
   ```

4. Start the server:
   ```bash
   npm start
   ```

The backend server will run on `http://localhost:3000`.

### Android Connection:
- In the Android Emulator, use `http://10.0.2.2:3000` (which points to `localhost:3000` of the host computer).
- On physical Android devices, use your computer's local Wi-Fi IP address (e.g. `http://192.168.1.50:3000`) or a deployed cloud server URL.
- You can configure this directly inside the **Settings** screen of the Nova app.
