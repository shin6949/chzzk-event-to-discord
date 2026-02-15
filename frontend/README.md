# Frontend Scaffold

React + Vite + TypeScript frontend scaffold for the Chzzk Event to Discord app.

## Prerequisites
- Node.js 20+
- Yarn 1.x

## Environment
Create local env values from the example file:

```bash
cp .env.example .env.local
```

`VITE_API_BASE_URL` configures the backend API base URL.

## Local development

```bash
yarn install
yarn dev
```

Default local URL: `http://localhost:5173`

## Unit tests

```bash
yarn test
```

Vitest is configured with React Testing Library in `src/test/setup.ts`.

## Build

```bash
yarn build
```

## E2E placeholder (Playwright)

```bash
yarn test:e2e
```

A smoke test is provided at `e2e/smoke.spec.ts` and does not require external secrets.

If Playwright browsers are not installed in the environment, install Chromium first:

```bash
yarn playwright install chromium
```

When browsers are unavailable (for example on restricted runners), `test:e2e` can be skipped and should not block FE scaffold cleanup.

Some sandboxed environments also block binding local ports; in that case Playwright `webServer` startup fails and E2E should be run in a normal local/CI runner instead.
