# 🏦 Synthetic Banking Data Generator (FraudGen)

A lightweight, high-performance REST API built with Kotlin and Ktor to generate synthetic, highly realistic banking data (accounts and transactions). 

Designed to solve the "Cold Start" and Privacy (GDPR) problems in Fintech, this tool provides developers, QA engineers, and Data Scientists with millions of PII-free data points to train Machine Learning models (like Fraud Detection systems) or run load tests, without exposing real customer data.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/ktor-%23087CFA.svg?style=for-the-badge&logo=ktor&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

> **Note:** [Insert an animated GIF here showing the Swagger UI execution and Kafka UI monitoring]

## ✨ Key Features

* **Behavioral Simulation Engine:** Uses Markov Chains to generate realistic transaction sequences based on predefined psychological personas (e.g., "The Student", "The Gambler", "The Salaryman").
* **Real-Time Data Streaming:** Integrated with Apache Kafka to simulate live POS/ATM traffic. Uses Kotlin Coroutines to manage asynchronous background streaming with configurable TPS (Transactions Per Second).
* **Multi-Currency & FX Rates:** Supports international transactions (`EUR`, `USD`, `GBP`, `JPY`, `CHF`) with an internal Foreign Exchange engine calculating base amounts.
* **Custom Kotlin DSL:** A type-safe, elegant Domain Specific Language (`@DslMarker`) to configure data generation rules dynamically.
* **Zero-Reflection Serialization:** Uses `kotlinx-serialization` for compile-time JSON encoding, drastically reducing memory overhead.
* **API-First Design:** Fully documented with OpenAPI 3.0 and bundled with an interactive Swagger UI.

## 🛠️ Tech Stack

* **Language:** Kotlin 2.2.0
* **Framework:** Ktor Server 2.3.10 (Netty)
* **Message Broker:** Apache Kafka 3.7.0 (KRaft mode)
* **Data Mocking:** Datafaker 2.1.0
* **Serialization:** `kotlinx-serialization-json` 1.6.3
* **Testing:** JUnit 5 (5.10.2) & MockK (1.13.10)
* **Infrastructure:** Docker & Docker Compose

## 🚀 Getting Started

### Prerequisites
* Java JDK 21 installed.
* Docker Desktop (for Kafka infrastructure).

### Installation & Run

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/fraud-data-generator.git](https://github.com/yourusername/fraud-data-generator.git)
   cd fraud-data-generator
   ```

2. Start the Kafka Infrastructure (runs in background):
   ```bash
   docker-compose up -d
   ```
   *You can monitor Kafka topics by opening Kafka UI at `http://localhost:8081`.*

3. Run the Ktor application locally using the Gradle wrapper:
   ```bash
   ./gradlew run
   ```
   *The Ktor Netty server will start on port `8080`.*

## 📖 Usage

### Interactive API (Swagger UI)
Open your favorite browser and navigate to:
👉 **`http://localhost:8080/swagger`**

### Endpoint 1: Batch Generation (`/api/v1/generate`)
Generates data instantly and exports it to JSON and CSV files in the project root.

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/generate' \
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

---

## 🎯 TODO List (Future Improvements)

This project is continuously evolving. Here are the top planned improvements to make it even more enterprise-ready:

- [x] **1. Dockerization:** Create a `Dockerfile` and `docker-compose.yml` to run the application and infrastructure.
- [x] **2. Advanced ML Personas:** Implement Markov Chains to simulate realistic user behaviors instead of purely random distributions.
- [x] **3. Kafka Integration:** Add an endpoint to stream generated transactions in real-time to an Apache Kafka topic.
- [x] **4. Multi-Currency & FX Rates:** Support different currencies and simulate real-world Foreign Exchange rate conversions.
- [ ] **5. Database Sink:** Replace the basic file exporter (JSON/CSV) with an interface to inject data directly into a PostgreSQL or MongoDB database.
- [ ] **6. Comprehensive Test Suite:** Add Unit Tests using `JUnit 5` and `MockK`, and implement Ktor Server Tests (`testApplication`) for the API endpoints.
- [ ] **7. CI/CD Pipeline:** Set up GitHub Actions to automatically build, test, and lint the code on every push or Pull Request.
- [ ] **8. Centralized Configuration:** Migrate hardcoded default values to an `application.conf` (HOCON) or `.env` file for easier environment management.
- [ ] **9. Structured Logging:** Integrate `kotlin-logging` and `Logback`, formatting logs in JSON format for easy ingestion by an ELK stack.
- [ ] **10. Authentication Layer:** Secure the endpoints using API Keys or JWT tokens to prevent unauthorized data generation in a deployed environment.