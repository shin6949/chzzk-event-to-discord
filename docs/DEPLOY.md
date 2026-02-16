# Production-ish Deploy Guide

This repository supports separated frontend and backend containers for local/prod-ish runs.

## Required environment variables

Backend (Spring Boot) uses environment variables prefixed with `APP_` and `CHZZK_`:

- `APP_DB_DRIVER` (required): JDBC driver class, e.g. `org.postgresql.Driver`
- `APP_DB_URL` (required): JDBC URL
- `APP_DB_USER` (required): DB user
- `APP_DB_PASSWORD` (required): DB password
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

Frontend container env:

- `VITE_API_BASE_URL`: base URL for frontend API calls.
  - In docker-compose on same network: `http://backend:8080/api/v1`
  - For external public access, set to the reachable backend base URL.

## Run with Docker Compose

From repository root:

```bash
export APP_DB_DRIVER=org.postgresql.Driver
export APP_DB_URL=jdbc:postgresql://<db-host>:5432/<db-name>
export APP_DB_USER=<user>
export APP_DB_PASSWORD=<password>
export CHZZK_OAUTH_CLIENT_ID=<client id>
export CHZZK_OAUTH_CLIENT_SECRET=<client secret>
export CHZZK_OAUTH_REDIRECT_URI=<redirect uri>

# optional but recommended for FE API endpoint in the container network
export VITE_API_BASE_URL=http://backend:8080/api/v1

docker compose up --build
```

Containers:

- `backend`: exposes port 8080
- `frontend`: serves static files with Nginx, exposes port 80 internally and maps to host port 3000 by default

## Flyway / DB schema note (optional)

If your deployment pipeline uses `flyway`, a baseline migration is provided for
`chzzk_oauth_token` at `src/main/resources/db/migration/V1__create_chzzk_oauth_token.sql`.
