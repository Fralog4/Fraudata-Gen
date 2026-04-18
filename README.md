# 🏦 Synthetic Banking Data Generator (FraudGen)

A lightweight, high-performance REST API built with Kotlin and Ktor to generate synthetic, highly realistic banking data (accounts and transactions). 

Designed to solve the "Cold Start" and Privacy (GDPR) problems in Fintech, this tool provides developers, QA engineers, and Data Scientists with millions of PII-free data points to train Machine Learning models (like Fraud Detection systems) or run load tests, without exposing real customer data.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/ktor-%23087CFA.svg?style=for-the-badge&logo=ktor&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)

> **Note:** [Insert an animated GIF here showing the Swagger UI execution and the generated JSON files]

## ✨ Key Features

* **Custom Kotlin DSL:** A type-safe, elegant Domain Specific Language (`@DslMarker`) to configure data generation rules dynamically.
* **Asynchronous & Non-Blocking:** Powered by Kotlin Coroutines and the Ktor Netty engine for lightning-fast concurrent requests.
* **Zero-Reflection Serialization:** Uses `kotlinx-serialization` for compile-time JSON encoding, drastically reducing memory overhead compared to traditional Java libraries like Jackson.
* **API-First Design:** Fully documented with OpenAPI 3.0 and bundled with an interactive Swagger UI.
* **Domain-Driven:** Immutability by default (`data class`), Type-Safety (`value class`), and strict separation between the API layer and the generation engine.

## 🛠️ Tech Stack

* **Language:** Kotlin 1.9+
* **Framework:** Ktor Server (Netty)
* **Data Mocking:** Datafaker
* **Serialization:** `kotlinx-serialization-json`
* **Build Tool:** Gradle (Kotlin DSL)

## 🚀 Getting Started

### Prerequisites
* Java JDK 17 or higher installed.

### Installation & Run

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/fraud-data-generator.git](https://github.com/yourusername/fraud-data-generator.git)
   cd fraud-data-generator
   ```

2. Run the application using the Gradle wrapper:
   ```bash
   ./gradlew run
   ```

3. The Ktor Netty server will start on port `8080`.

## 📖 Usage

### Interactive API (Swagger UI)
Open your favorite browser and navigate to:
👉 **`http://localhost:8080/swagger`**

From there, you can interact with the `POST /api/v1/generate` endpoint, tweak the generation parameters, and execute the request.

### Example API Request
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/generate' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "accountsCount": 50,
  "countryCode": "GB",
  "transactionsPerAccount": 20,
  "fraudProbability": 3.5
}'
```

### Output
The application returns a JSON response with the operation summary and automatically exports the generated data into the root folder as `accounts.json`, `transactions.json`, `accounts.csv`, and `transactions.csv`.

---

## 🎯 TODO List (Future Improvements)

This project is continuously evolving. Here are the top 10 planned improvements to make it even more enterprise-ready:

- [ ] **1. Dockerization:** Create a `Dockerfile` and `docker-compose.yml` to run the Ktor application and Swagger UI inside an isolated container with zero setup. DONE
- [ ] **2. Advanced ML Personas:** Implement Markov Chains to simulate realistic user behaviors (e.g., "The Student", "The Gambler", "The Salaryman") instead of purely random distributions. DONE 
- [ ] **3. Kafka Integration:** Add an endpoint to stream generated transactions in real-time to an Apache Kafka topic, simulating a live banking environment. DONE
- [ ] **4. Multi-Currency & FX Rates:** Support different currencies (`USD`, `EUR`, `GBP`) and simulate real-world Foreign Exchange rate conversions within the transactions.
- [ ] **5. Database Sink:** Replace the basic file exporter (JSON/CSV) with an interface to inject data directly into a PostgreSQL or MongoDB database.
- [ ] **6. Comprehensive Test Suite:** Add Unit Tests using `JUnit 5` and `MockK`, and implement Ktor Server Tests (`testApplication`) for the API endpoints.
- [ ] **7. CI/CD Pipeline:** Set up GitHub Actions to automatically build, test, and lint the code on every push or Pull Request.
- [ ] **8. Centralized Configuration:** Migrate hardcoded default values to an `application.conf` (HOCON) or `.env` file for easier environment management.
- [ ] **9. Structured Logging:** Integrate `kotlin-logging` and `Logback`, formatting logs in JSON format for easy ingestion by an ELK (Elasticsearch, Logstash, Kibana) stack.
- [ ] **10. Authentication Layer:** Secure the `/generate` endpoint using API Keys or JWT tokens to prevent unauthorized data generation in a deployed environment.