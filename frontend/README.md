# PARS React frontend

This React/Vite app signs in through the Spring Boot API and loads the current
user from `/api/v1/auth/me`. During development, Vite proxies `/api` requests to
`http://localhost:8080`.

Run the backend on port 8080, then from this directory run:

```sh
npm install
npm run dev
```

Open the URL printed by Vite (by default `http://localhost:5173`). Access and
refresh tokens are kept in the current browser tab's session storage; expired
access tokens are refreshed through `/api/v1/auth/refresh`.

> **Login returns 403 "Invalid CORS request"?** The backend allows only the
> origins listed in `pars.security.cors.allowed-origins`. The `dev` profile
> defaults to `http://localhost:5173` (matching Vite). If you run Vite on a
> different port, start Spring Boot with
> `PARS_CORS_ORIGINS=http://localhost:<your-port>` and restart the backend —
> the allow-list is read only at startup.

## Production

Build the static frontend with `npm run build` and serve the contents of `dist`
over HTTPS. Configure the web server to serve the frontend files and proxy
`/api` requests to the Spring Boot backend; the frontend uses relative API
paths, so both should be available on the same origin.

Run the backend with the `prod` Spring profile and set `PARS_DB_URL`,
`PARS_DB_USER`, `PARS_DB_PASSWORD`, `PARS_JWT_SECRET`, and `PARS_CORS_ORIGINS`
in its environment. `PARS_CORS_ORIGINS` must contain the exact HTTPS origin(s)
of the deployed frontend, such as `https://pars.example.org`; do not use `*`.
Use a private, randomly generated JWT secret of at least 256 bits, and provide
a persistent writable `PARS_DEAL_ATTACHMENT_DIR` for uploaded files. The
production profile intentionally requires the CORS allow-list instead of
allowing requests from every origin.
