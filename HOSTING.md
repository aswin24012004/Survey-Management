# Permanent Render hosting

The root `Dockerfile` packages React inside the Spring Boot application. Both are
served on one HTTPS origin. `render.yaml` provisions this service and a separate
persistent PostgreSQL database in Singapore. These are paid resources: review
the displayed charges in Render before applying the Blueprint.

## Deploy

1. Push the reviewed source changes (including the new hosting files) to the
   GitHub repository. Do not commit environment files, logs, or build outputs.
2. Connect the repository in Render and create a Blueprint using `render.yaml`.
3. Review the service and database plans and approve creation in Render.
4. Wait for the Docker build, database migrations, and `/healthz` check to pass.
5. Open the service's HTTPS URL and log in as `admin`. Retrieve the generated
   `ADMIN_INITIAL_PASSWORD` privately from Render's environment settings.
6. Verify login, survey creation, publishing, submission, and response retrieval.

Render generates the initial admin password and JWT secret. Existing non-default
admin passwords are preserved on restart. The production profile requires a
PostgreSQL connection; it does not fall back to an in-memory database. Flyway
manages schema changes and Hibernate validates the schema.

For a custom domain, set `PUBLIC_APP_URL` to its HTTPS origin (without a trailing
slash) and redeploy after adding the domain in Render. Otherwise the app uses
Render's supplied `RENDER_EXTERNAL_URL` for public survey links and CORS.

Automatic deployment is disabled so source pushes do not unexpectedly change the
public app. Trigger subsequent deployments explicitly after testing.

## Local checks and remaining verification

The backend unit/controller tests and frontend build can run without cloud
credentials. A Docker build and a fresh PostgreSQL migration check must also pass
before treating the deployment as verified. Existing Neon integration tests use
an external database and should not be run against production for smoke tests.

The broader review identified refresh-token revocation and response-count
concurrency issues that are not addressed by this hosting setup.

Reference: https://render.com/docs/blueprint-spec
