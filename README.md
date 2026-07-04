### How do I get set up? ###

This is a Maven multi-module project:

* `back-app` — Spring Boot (Java 17) REST API
* `front-app` — React Admin dashboard (Create React App)

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

### Frontend (`front-app`)

Create React App natively reads `front-app/.env` (only `REACT_APP_*` variables
are exposed to the browser bundle).

```bash
cp front-app/.env.example front-app/.env  # then edit and fill real values
npm install --legacy-peer-deps
npm start
```

### Deployment (Google App Engine)

```bash
mvn clean install appengine:deploy -Pstaging   # staging
mvn clean install appengine:deploy -Pprod      # prod
```
