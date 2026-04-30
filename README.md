# 🏦 Synthetic Banking Data Generator (FraudataGen)

A lightweight, high-performance REST API built with Kotlin and Ktor to generate synthetic, highly realistic banking data (accounts and transactions). 

Designed to solve the "Cold Start" and Privacy (GDPR) problems in Fintech, this tool provides developers, QA engineers, and Data Scientists with millions of PII-free data points to train Machine Learning models (like Fraud Detection systems) or run load tests, without exposing real customer data.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/ktor-%23087CFA.svg?style=for-the-badge&logo=ktor&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/postgresql-4169e1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

> **Note:** [Insert an animated GIF here showing the Swagger UI execution and Kafka UI monitoring]

## ✨ Key Features

* **Behavioral Simulation Engine:** Uses Markov Chains to generate realistic transaction sequences based on predefined psychological personas (e.g., "The Student", "The Gambler", "The Salaryman").
* **Real-Time Data Streaming:** Integrated with Apache Kafka to simulate live POS/ATM traffic. Uses Kotlin Coroutines to manage asynchronous background streaming with configurable TPS (Transactions Per Second).
* **Multi-Currency & FX Rates:** Supports international transactions (`EUR`, `USD`, `GBP`, `JPY`, `CHF`) with an internal Foreign Exchange engine calculating base amounts.
* **Persistent Database Sink:** Automatically saves generated accounts and transactions into a relational PostgreSQL database via JetBrains Exposed ORM.
* **API Security:** Endpoints are protected via API Key Bearer authentication to prevent unauthorized data generation.
* **Custom Kotlin DSL:** A type-safe, elegant Domain Specific Language (`@DslMarker`) to configure data generation rules dynamically.
* **API-First Design:** Fully documented with OpenAPI 3.0 and bundled with an interactive Swagger UI.

## 🛠️ Tech Stack

* **Language:** Kotlin 2.2.0
* **Framework:** Ktor Server 2.3.10 (Netty)
* **Message Broker:** Apache Kafka 3.7.0 (KRaft mode)
* **Database & ORM:** PostgreSQL 16 & JetBrains Exposed
* **Data Mocking:** Datafaker 2.1.0
* **Serialization:** `kotlinx-serialization-json` 1.6.3
* **Logging:** `kotlin-logging` & Logback (JSON Structured Logs)
* **Testing:** JUnit 5 (5.10.2) & MockK (1.13.10)
* **Infrastructure:** Docker & Docker Compose

## 🚀 Getting Started

### Prerequisites
* Java JDK 21 installed.
* Docker Desktop (for Kafka & PostgreSQL infrastructure).

### Installation & Run

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/fraud-data-generator.git](https://github.com/yourusername/fraud-data-generator.git)
   cd fraud-data-generator
   ```

2. Configure environment variables:
   Create a `.env` file in the root directory and add your secret keys (e.g., `API_SECRET_KEY=YourSecretKey`, `DB_PASSWORD=YourDbPass`).

3. Start the Infrastructure (Kafka & Postgres runs in background):
   ```bash
   docker-compose up -d
   ```
   *You can monitor Kafka topics by opening Kafka UI at `http://localhost:8081`.*

4. Run the Ktor application locally using the Gradle wrapper:
   ```bash
   ./gradlew run
   ```
   *The Ktor Netty server will start on port `8080`.*

## 📖 Usage

### Interactive API (Swagger UI)
Open your favorite browser and navigate to:
👉 **`http://localhost:8080/swagger`**
*(Remember to click "Authorize" and input your API Key to unlock the endpoints!)*

### Endpoint 1: Batch Generation (`/api/v1/generate`)
Generates data instantly, saves it to PostgreSQL, and exports it to JSON/CSV files.

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/generate' \
  -H 'Authorization: Bearer <YOUR_API_KEY>' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "accountsCount": 50,
  "countryCode": "GB",
  "transactionsPerAccount": 20,
  "persona": "Gambler"
}'
```

### Endpoint 2: Live Streaming (`/api/v1/stream`)
Fires an asynchronous background job that streams generated transactions to the Kafka `transactions` topic mimicking real-time traffic.

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/stream' \
  -H 'Authorization: Bearer <YOUR_API_KEY>' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "accountsCount": 5,
  "countryCode": "US",
  "transactionsPerAccount": 50,
  "persona": "Student",
  "tps": 10
}'
```