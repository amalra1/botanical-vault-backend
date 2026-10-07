# Botanical Vault Backend

REST API for **Acervo de Plantas**, a private library of transparent plant PNGs used in architectural collages. It handles data, authentication and image storage for [botanical-vault-frontend](https://github.com/amalra1/botanical-vault-frontend).

## Tech Stack

- **Java 21** + **Spring Boot 4.1** (Maven)
- **PostgreSQL 17**: Docker locally, Supabase in production
- **Flyway** for schema migrations
- **JWT** (JJWT) for single-user authentication
- **Image storage**: local disk in development, Google Cloud Storage in production
- **Hosting**: Google Cloud Run (`us-east1`)

## Prerequisites

- JDK 21 (`brew install --cask temurin@21`)
- Docker (local database and Testcontainers)

## Local development

```bash
cp .env.example .env
docker compose up -d db
set -a && source .env && set +a
./mvnw spring-boot:run
```

Check that the API and the database are up:

```bash
curl http://localhost:8080/health
```

To run the API inside Docker as well:

```bash
docker compose --profile app up -d --build
```

Uploaded images are written to `./storage` (git ignored) while `STORAGE_PROVIDER=local`.

## Tests

```bash
./mvnw verify
```

Integration tests start a throwaway PostgreSQL with Testcontainers, so Docker must be running.

## Database migrations

Migrations live in `src/main/resources/db/migration` as `V<n>__<snake_case_name>.sql` and run automatically on startup. Hibernate only validates the schema (`ddl-auto=validate`). Never edit a migration that has already run in production; add a new one.

## Deploy

Every push to `main` triggers Cloud Build (`cloudbuild.yaml`), which builds the image, pushes it to Artifact Registry and deploys it to Cloud Run. There is only one environment: production.

### One-time GCP setup

Everything stays inside the GCP free tier. Use `us-east1` for every resource, because Cloud Storage's free tier only covers `us-east1`, `us-west1` and `us-central1`.

1. Create the Artifact Registry repository and keep only the latest images (the free tier covers 0.5 GB):
   ```bash
   gcloud artifacts repositories create botanical-vault --repository-format=docker --location=us-east1
   gcloud artifacts repositories set-cleanup-policies botanical-vault --location=us-east1 --policy=cleanup-policy.json
   ```
   `cleanup-policy.json` (not committed):
   ```json
   [{"name": "keep-latest", "action": {"type": "Keep"}, "mostRecentVersions": {"keepCount": 2}},
    {"name": "delete-rest", "action": {"type": "Delete"}, "condition": {"tagState": "ANY"}}]
   ```
2. Create a Cloud Build trigger on pushes to `main` that uses `cloudbuild.yaml`.
3. Create the bucket: `gcloud storage buckets create gs://<bucket> --location=us-east1 --uniform-bucket-level-access`.
4. Store `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` and `APP_ADMIN_PASSWORD` in Secret Manager (6 active versions are free) and expose them to the Cloud Run service, together with the plain env vars from `.env.example`.
5. Create one Cloud Scheduler job that calls `GET /health` every 3 days, so the Supabase free project is never paused for inactivity.
6. Add a budget alert in Billing.

### Supabase

Create the project in `us-east-1` (closest to `us-east1`). Cloud Run has no IPv6 egress, so connect through the **Session pooler**:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
```

## Documentation

- **Architecture, conventions and domain rules**: `.claude/CLAUDE.md` (local only)
- **Screens**: https://claude.ai/artifact/Li6BVKX6joyN2dqmXxFXLo
