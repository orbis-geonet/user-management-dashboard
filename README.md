# Orbis Admin Dashboard

The internal admin / user-management dashboard for **[Orbis](https://orbis.social)** — a [geo-social network](https://en.wikipedia.org/wiki/Geosocial_networking) where communities claim real-world places and grow their tribe's territory on the map. This is the tool the Orbis team uses to operate the network: manage users, tribes, places and posts, moderate flagged content, track growth statistics, and run email campaigns.

It is a Maven multi-module project:

| Module | What it is |
|---|---|
| [`back-app`](./back-app) | Spring Boot (Java 17) REST API — `/api/v1`, backed by MongoDB |
| [`front-app`](./front-app) | [react-admin](https://marmelab.com/react-admin/) single-page app (Create React App, React 17) |

## What it does

The dashboard exposes CRUD screens and operations over the core entities of the Orbis network, plus a set of admin-only tools:

- **Users & user sets** — browse, search, edit and group user accounts; follow relationships; bulk CSV import/export.
- **Tribes (groups) & places** — manage tribe pages and real-world places, with Google Maps fields and territory polygon recalculation.
- **Posts & feed** — inspect and moderate user posts, including admin posting to the main app.
- **Moderation** — review user **reports** and **flagged entities** (users / posts / places), resolve or delete offending content.
- **Partners** — manage partner records and per-partner statistics.
- **Statistics** — growth and activity metrics with bar / pie chart views and configurable periods.
- **Email campaigns** — a small bulk-mailing system on top of **AWS SES**: campaigns, tags, per-recipient status, open tracking (via the SES `open_and_click` configuration set + SNS notifications), test mode, drafts, resend, and an unsubscribe flow. A scheduled task (`EmailCampaignTasks`) drains active campaigns continuously.
- **Sitemap generation** — triggers the dynamic sitemap build for [orbis.social](https://orbis.social) (see [dynamic-site-map](https://github.com/orbis-geonet/dynamic-site-map)).
- **Media** — image upload / crop / resize to Google Cloud Storage and Firebase Storage; Google Drive export for reports.

### How authentication works

Admin accounts are regular Orbis users flagged as **super-admin** in MongoDB. Login (`POST /api/v1/auth/login`) verifies the password against **Firebase Identity Toolkit** (`signInWithPassword`), then the backend issues its own HS256 JWT (signed with `JWT_SECRET`) that the SPA sends as a `Bearer` token. Everything under `/api/**` requires that token, except the login endpoint, `/api/v1/keep-alive`, and `/api/v1/public/**`.

An RSA JWK (`prk.rsa`) is used to mint Firebase custom server tokens when the dashboard needs to act against the main Orbis backend.

## How do I get set up?

### Backend (`back-app`)

Secrets are read from `back-app/.env` at startup. A small
`DotenvEnvironmentPostProcessor` (registered in
`back-app/src/main/resources/META-INF/spring.factories`, backed by the
`io.github.cdimascio:dotenv-java` dependency) loads the file into Spring's
environment, so every `${VAR}` placeholder in `application*.yaml` resolves from
it. Real OS environment variables always take precedence over `.env`.

Setup:

```bash
cp back-app/.env.example back-app/.env   # then edit and fill real values
```

Start a local MongoDB (or use the provided compose file):

```bash
docker-compose up -d mongodb
```

Run locally (the app finds `.env` in the working dir or a parent):

```bash
cd back-app
mvn spring-boot:run -Dspring-boot.run.profiles=prod-local   # pick a profile
```

Variables (see `back-app/.env.example`): `MONGODB_URI`, `JWT_SECRET`,
`AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `GOOGLE_API_KEY`, `FIREBASE_API_KEY`,
`GOOGLE_PLACE_API_KEY`.

Two credential **files** are also required and git-ignored — copy the provided
templates and drop in your own:

```bash
cp back-app/src/main/resources/prk.rsa.example \
   back-app/src/main/resources/prk.rsa                       # JWT RSA signing key (JWK)
cp back-app/src/main/resources/google/company-credentials.json.example \
   back-app/src/main/resources/google/company-credentials.json  # GCP service account
```

Spring profiles: `staging`, `prod`, and `prod-local` (production data, local
tweaks, dev tasks enabled) — see `back-app/src/main/resources/application-*.yaml`.

### Frontend (`front-app`)

Create React App natively reads `front-app/.env` (only `REACT_APP_*` variables
are exposed to the browser bundle).

```bash
cp front-app/.env.example front-app/.env  # then edit and fill real values
npm install --legacy-peer-deps
npm start
```

The dev server runs on http://localhost:3000 and talks to the backend at
`REACT_APP_BACK_URL` (default `http://localhost:8080/api/v1`).

### Deployment (Google App Engine)

The backend deploys as the `admin` service of the Orbis GCP projects
(`back-app/src/main/appengine*/app.yaml`). Deploy-time environment variables in
those files are placeholders — inject real values in your deploy pipeline,
never commit them.

```bash
mvn clean install appengine:deploy -Pstaging   # staging
mvn clean install appengine:deploy -Pprod      # prod
```

## Security notes

- No real credentials live in this repository — `.env`, `prk.rsa` and
  `company-credentials.json` are git-ignored; only `*.example` templates with
  dummy values are committed. Keep it that way: this repo is public.
- CSRF protection is disabled because the API is stateless and token-based;
  sessions, form login and logout are disabled too.
- Only super-admin users can log in; everything except login, keep-alive and
  `/api/v1/public/**` requires a valid JWT.

## Related repositories

- [website](https://github.com/orbis-geonet/website) — public web client (orbis.social)
- [android-app](https://github.com/orbis-geonet/android-app) — native Android client
- [dynamic-site-map](https://github.com/orbis-geonet/dynamic-site-map) — sitemap generator fed by this dashboard

## License

[AGPL-3.0](./LICENSE)
