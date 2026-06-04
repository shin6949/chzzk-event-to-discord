# Production-ish Deploy Guide

This repository supports separated frontend and backend containers for local/prod-ish runs.

## Required environment variables

Backend (Spring Boot) uses environment variables prefixed with `APP_` and `CHZZK_`:

- `APP_DB_DRIVER`: JDBC driver class, defaults to `org.postgresql.Driver` in docker-compose
  - PostgreSQL 17.4: `org.postgresql.Driver`
  - Oracle DB 21c: `oracle.jdbc.OracleDriver`
- `APP_DB_URL`: JDBC URL, defaults to `jdbc:postgresql://postgres:5432/chzzk_event_to_discord` in docker-compose
  - PostgreSQL 17.4: `jdbc:postgresql://postgres:5432/chzzk_event_to_discord`
  - Oracle DB 21c: `jdbc:oracle:thin:@//<host>:1521/<service_name>`
- `APP_DB_USER`: DB user, defaults to `chzzk` in docker-compose
- `APP_DB_PASSWORD`: DB password, defaults to `chzzk` in docker-compose
- `APP_DB_NAME`: Postgres database name for docker-compose, defaults to `chzzk_event_to_discord`
- `APP_DB_MAX_POOL_SIZE`, `APP_DB_MIN_IDLE`, `APP_DB_CONNECTION_TIMEOUT_MS`,
  `APP_DB_IDLE_TIMEOUT_MS`, `APP_DB_MAX_LIFETIME_MS`, `APP_DB_KEEPALIVE_TIME_MS`:
  optional Hikari pool settings. Defaults are tuned to avoid fixed-size pool and
  keepalive/max-lifetime warnings in compose.
- `CHZZK_OAUTH_CLIENT_ID` (required): OAuth client ID
- `CHZZK_OAUTH_CLIENT_SECRET` (required): OAuth client secret
- `CHZZK_OAUTH_REDIRECT_URI` (required): OAuth callback URI registered in Chzzk
- `CHZZK_OAUTH_AUTH_BASE_URL` (optional): maps to `chzzk.oauth.auth-base-url`  
  (default: `https://chzzk.naver.com`)
- `CHZZK_OAUTH_TOKEN_BASE_URL` (optional): maps to `chzzk.oauth.token-base-url`  
  (default: `https://openapi.chzzk.naver.com`)
- `CHZZK_OAUTH_API_BASE_URL` (optional): maps to `chzzk.oauth.api-base-url`  
  (default: `https://openapi.chzzk.naver.com`)

Optional backend variables:

- `CHZZK_CHECK_INTERVAL`: polling interval seconds (`CHZZK_CHECK_INTERVAL`)
- `CHZZK_API_URL`: public CHZZK API URL override (`chzzk.api-url`)
- `APP_INSERT_PASSWORD`: legacy insert API password
- `APP_DEFAULT_TIMEZONE`, `APP_IS_TEST`
- `APP_AUTH_JWT_SECRET`: HMAC signing secret for application access/refresh JWTs. Use a high-entropy value of at least 32 bytes and keep it stable across backend restarts.
- `APP_AUTH_JWT_ISSUER`, `APP_AUTH_JWT_ACCESS_TOKEN_TTL`, `APP_AUTH_JWT_REFRESH_TOKEN_TTL`,
  `APP_AUTH_OAUTH_STATE_TTL`, `APP_AUTH_COOKIE_SECURE`, `APP_AUTH_COOKIE_SAME_SITE`:
  optional JWT and auth cookie settings. Set `APP_AUTH_COOKIE_SECURE=true` when serving over HTTPS.
- `APP_STATIC_CONTENT_URL_PREFIX`: display URL prefix for uploaded images. Set this when images are served through a web server/CDN/proxy. If omitted, the backend falls back to `<APP_STORAGE_S3_ENDPOINT>/<bucket>/<objectKey>`.

S3-compatible object storage:

- `APP_STORAGE_S3_ENDPOINT`: S3 API endpoint. Local compose defaults to `http://minio:9000`; OCI uses `https://<namespace>.compat.objectstorage.<region>.oraclecloud.com`.
- `APP_STORAGE_S3_BUCKET`: object bucket name, defaults to `chzzk-event-assets` in docker-compose
- `APP_STORAGE_S3_REGION`: signing region
- `APP_STORAGE_S3_ACCESS_KEY`, `APP_STORAGE_S3_SECRET_KEY`: S3-compatible credentials
- `APP_STORAGE_S3_FORCE_PATH_STYLE`: defaults to `true` for MinIO and OCI path-style compatibility
- `APP_STORAGE_S3_DISABLE_CHUNKED_ENCODING`: defaults to `true` for OCI S3 compatibility
- `APP_STORAGE_S3_PREFIX`: object key prefix for bot profile uploads, defaults to `bot-profiles`
- `APP_STORAGE_S3_CONNECTION_TIMEOUT_MS`, `APP_STORAGE_S3_SOCKET_TIMEOUT_MS`,
  `APP_STORAGE_S3_API_CALL_ATTEMPT_TIMEOUT_MS`, `APP_STORAGE_S3_API_CALL_TIMEOUT_MS`:
  S3-compatible client timeout controls. Defaults are `5000`, `60000`,
  `120000`, `120000`. The client disables `Expect: 100-continue` and uses
  these bounded timeouts so local MinIO/OCI failures return without hanging.
- `APP_STORAGE_S3_AVAILABILITY_CHECK_ENABLED`: when `true`, backend probes object
  storage on startup and periodically.
- `APP_STORAGE_S3_REQUIRED_ON_STARTUP`: when `true`, backend startup fails if the
  startup object storage probe fails. Default is `false`, so only upload
  features are disabled while the rest of the app stays available.
- `APP_STORAGE_S3_AVAILABILITY_CHECK_INTERVAL_MS`,
  `APP_STORAGE_S3_AVAILABILITY_CHECK_TIMEOUT_MS`: periodic probe interval and
  per-probe timeout. Defaults are `60000` and `5000`.
- `APP_STORAGE_UPLOAD_MAX_BYTES`, `APP_STORAGE_UPLOAD_ALLOWED_TYPES`: upload validation settings

Frontend build config:

- `VITE_API_BASE_URL`: base URL for frontend API calls.
  - In docker-compose, default to `/api/v1` so browser requests stay same-origin.
  - The frontend Nginx container proxies `/api/` to `http://backend:8080`.
  - Because this is a Vite build-time value, rebuild the frontend image after changing it.
  - With the default compose ports, register the Chzzk callback against the frontend origin, e.g. `http://localhost:3000/api/v1/auth/chzzk/callback`.

## Run with Docker Compose

From repository root:

```bash
cp .env.example .env
# edit .env and set at least CHZZK_OAUTH_CLIENT_ID, CHZZK_OAUTH_CLIENT_SECRET,
# CHZZK_OAUTH_REDIRECT_URI, APP_DB_PASSWORD, and APP_AUTH_JWT_SECRET

docker compose up --build
```

Containers:

- `postgres`: PostgreSQL 17.4, stores data in the `postgres-data` Docker volume, and is only reachable inside the Compose network
- `minio`: local/test S3-compatible object storage, exposes API port 9000 and console port 9001
- `minio-bootstrap`: creates the asset bucket and grants anonymous download access for `bot-profiles/`
- `backend`: listens on port 8080 inside the Compose network; frontend Nginx proxies `/api/` to it
- `frontend`: serves static files with Nginx, exposes port 80 internally and maps to host port 3000 by default

For local compose, uploaded bot profile images are stored in MinIO and shown through:

```text
APP_STATIC_CONTENT_URL_PREFIX=http://localhost:9000/chzzk-event-assets
```

For OCI production, keep the storage endpoint pointed at OCI Object Storage and set
`APP_STATIC_CONTENT_URL_PREFIX` to the public web server/CDN/proxy URL that should be
sent to the frontend and Discord.

## Flyway / DB schema note

The backend runs Flyway automatically on startup. Schema migrations are split by
database vendor under `src/main/resources/db/migration/{vendor}`:

- `postgresql`: PostgreSQL 17.4
- `oracle`: Oracle DB 21c
- `h2`: tests

Hibernate validates the resulting schema with `ddl-auto=validate`.
