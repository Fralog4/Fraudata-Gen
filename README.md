# Fraudata-Gen

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/ktor-%23087CFA.svg?style=for-the-badge&logo=ktor&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/postgresql-4169e1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

Ktor/Kotlin service for generating synthetic banking data and optionally streaming transactions to Kafka.

The project exposes authenticated REST endpoints, persists generated data to PostgreSQL, exports JSON/CSV files, and can publish transactions to a Kafka topic (`transactions`) at a controlled TPS rate.

## Tech Stack

- Kotlin 2.2.0
- Ktor 2.3.10 (Netty)
- PostgreSQL 16 + Exposed ORM + HikariCP
- Apache Kafka 3.7.0
- Docker/Podman Compose

## Current API Status

Implemented endpoints:

- `POST /api/v1/generate` (auth required)
- `POST /api/v1/stream` (auth required)
- `GET /swagger`

## Runtime Architecture

- `fraudgen-api` (port `8080`): main API service
- `db` (port `5432`): PostgreSQL
- `kafka` (port `9092`): broker
- `kafka-ui` (port `8081`): topic/message inspection UI

## Prerequisites

- Java 21 (for local Gradle runs)
- Docker or Podman + Compose provider
- Ports available: `8080`, `8081`, `5432`, `9092`

## Quick Start (Compose)

From project root:

```powershell
podman compose -f docker-compose.yml -p fraudata-gen up -d --build
```

Check status:

```powershell
podman compose -f docker-compose.yml -p fraudata-gen ps
```

Open:

- Swagger UI: `http://localhost:8080/swagger`
- Kafka UI: `http://localhost:8081`

## Authentication

API endpoints are protected with Bearer auth.

- Default dev key (fallback): `default-dev-key`
- If `API_SECRET_KEY` is set in environment, use that value instead.

In Swagger `Authorize` popup, insert only the token value (without `Bearer ` prefix).

## Endpoints

### 1) `POST /api/v1/generate`

Generates synthetic accounts/transactions, writes to PostgreSQL, exports to files.

Request body:

```json
{
  "accountsCount": 50,
  "countryCode": "GB",
  "transactionsPerAccount": 20,
  "persona": "Gambler"
}
```

Response:

```json
{
  "message": "Data generated successfully",
  "totalAccounts": 50,
  "totalTransactions": 1000
}
```

Generated files are written to:

- `generated-data/accounts.json`
- `generated-data/transactions.json`
- `generated-data/accounts.csv`
- `generated-data/transactions.csv`

### 2) `POST /api/v1/stream`

Generates synthetic data and streams transactions to Kafka topic `transactions` at `tps`.

Request body:

```json
{
  "accountsCount": 5,
  "countryCode": "US",
  "transactionsPerAccount": 50,
  "persona": "Student",
  "tps": 10
}
```

Response (async acceptance):

```json
{
  "message": "Stream job accepted",
  "status": "RUNNING",
  "estimatedCompletionSeconds": 25
}
```

### Stream Job Behavior

The stream job is finite, not infinite.

- Total events = `accountsCount * transactionsPerAccount`
- Duration (approx) = `total events / tps` seconds
- Job completion is logged as `LIVE Kafka streaming completed.`

## Validation Rules (Current)

- `accountsCount > 0`
- `transactionsPerAccount > 0`
- `tps > 0` (stream only)

Invalid values return `400 Bad Request`.

## Environment Variables

Main variables used by the API container:

- `PORT` (default `8080`)
- `KAFKA_BOOTSTRAP_SERVERS`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `API_SECRET_KEY` (optional, overrides dev fallback)

## Operational Checks

### API logs

```powershell
podman logs fraudgen-api-container --tail 200
```

### Check PostgreSQL row counts

```powershell
podman exec -it fraudgen-postgres psql -U fraudgen -d fraudgen_db -c "select count(*) from accounts;"
podman exec -it fraudgen-postgres psql -U fraudgen -d fraudgen_db -c "select count(*) from transactions;"
```

### Check Kafka messages

Use Kafka UI:

1. Open `http://localhost:8081`
2. Select cluster
3. Open topic `transactions`
4. Inspect `Messages`

## Troubleshooting

### Swagger shows parser/indentation error

Cause: malformed `openapi.yml`.  
Fix: verify YAML indentation under `components.securitySchemes`.

### `404` on `/api/v1/generate` or `/api/v1/stream`

Cause: old container image still running.  
Fix:

```powershell
podman compose -f docker-compose.yml -p fraudata-gen up -d --build fraudgen-api
```

### API cannot connect to DB (`localhost:5432 refused`)

Cause: DB host in container must be service name, not localhost.  
Fix: ensure `DB_URL=jdbc:postgresql://db:5432/fraudgen_db`.

### `generated-data` is empty

Cause: API version not writing to mounted folder or request not executed after rebuild.  
Fix:

1. rebuild API container
2. call `/api/v1/generate`
3. refresh host folder `generated-data`

## Stop / Cleanup

Stop stack:

```powershell
podman compose -f docker-compose.yml -p fraudata-gen down
```

Stop and remove volumes:

```powershell
podman compose -f docker-compose.yml -p fraudata-gen down -v
```
