# Library System

A Spring Boot REST API for registering borrowers and managing library books. The
Docker Compose setup runs the API with a persistent MySQL 8.4 database and a
Redis-backed cache for the book catalogue.

## API documentation

- [Endpoint guide, examples, and diagrams](docs/api.md)
- [OpenAPI 3.1 specification](docs/openapi.yaml)

Interactive Swagger documentation is also available while the application is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Generated OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Generated OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

The generated documentation describes request validation, success responses,
business failures, and error responses for every API endpoint. If `APP_PORT` is
changed, replace `8080` in these URLs with that value.

## Prerequisites

- Docker Engine 27 or newer
- Docker Compose v2

Maven and Java are not required on the host when using Docker; the application is
built and tested inside a Java 17 multi-stage image.

## Configure the environment

Create a local environment file from the provided template:

```powershell
Copy-Item .env.example .env
```

On macOS or Linux, use `cp .env.example .env`. Change both password values in
`.env` before starting the stack. The local `.env` file is ignored by Git.

`CACHE_TTL` controls how long the book catalogue remains cached in Redis and
defaults to `30m`. When running the application outside Docker Compose, Redis
defaults to `localhost:6379`; override it with `REDIS_HOST` and `REDIS_PORT`.

## Run with Docker Compose

Build the image and start the API and database:

```shell
docker compose up --build -d
```

The API is available at `http://localhost:8080` by default. Change `APP_PORT` in
`.env` if that port is already in use.

View service status and logs:

```shell
docker compose ps
docker compose logs -f app
```

Stop the containers without deleting database data:

```shell
docker compose down
```

The MySQL data is stored in the `mysql-data` named volume and survives normal
container recreation. To intentionally delete all local database data:

```shell
docker compose down -v
```

## API smoke test

List all books after the stack has started:

```shell
curl http://localhost:8080/api/v1/books
```

Create a borrower:

```shell
curl -X POST http://localhost:8080/api/v1/borrowers \
  -H "Content-Type: application/json" \
  -d '{"name":"Ada Lovelace","email":"ada@example.com"}'
```

Create a book:

```shell
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"isbnNumber":"9780132350884","title":"Clean Code","author":"Robert C. Martin"}'
```

If `APP_PORT` was changed, replace `8080` in these commands with that value.

## Build and test without Docker

With Java 17 or newer and Maven installed:

```shell
mvn test
```
