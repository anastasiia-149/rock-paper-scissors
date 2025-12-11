# Rock Paper Scissors Game

## Architecture

This project implements **Hexagonal Architecture** (Ports and Adapters) with clean separation of concerns:

### Backend Structure
```
backend/
├── control/          # Core Business Logic (Domain Layer)
│   ├── model/        # Domain models
│   ├── ports/        # Port interfaces
│   └── exception/    # Domain exceptions
├── boundary/         # External Interactions
│   ├── incoming/     # REST controllers (what clients call)
│   └── outgoing/     # External adapters (what we call)
└── config/          # Configuration classes
```

### Frontend Structure
```
frontend/
├── components/       # UI Components
├── services/        # Services with Signals
├── models/          # TypeScript interfaces
└── app.config.ts    # Zoneless configuration
```

## Technology Stack

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.3.5**
- **Gradle 8.5**
- **OpenAPI 3.0** (for API-first design + code generation)
- **Lombok** 
- **Micrometer + Prometheus** (Metrics)
- **Spring Actuator** (Health checks & monitoring)
- **SLF4J + Logback** (Logging)

### Frontend
- **Angular 18**
- **TypeScript 5.5**
- **Zoneless Change Detection**
- **Signals** (Reactive state management)

## Prerequisites

- **Java 21** or higher
- **Node.js 18+** and **npm**
- **Gradle 8.5** or higher (wrapper included)
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse

## Quick Start

### 1. Clone the Repository

```bash
cd ..../test-project
```

### 2. Build the Backend

```bash
cd backend
./gradlew clean build
```

This will:
- Generate API interfaces and DTOs from OpenAPI specification
- Compile the application
- Run all tests

### 3. Start the Backend Server

```bash
./gradlew bootRun
```


The server will start on **http://localhost:8080**

### 4. Start the Frontend

In a new terminal window:

```bash
cd frontend
npm install
npm start
```

The frontend will start on **http://localhost:4200**

### 5. Verify Everything is Running

**Backend health check:**
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

**Frontend**: Open your browser to **http://localhost:4200**

You should see the Rock Paper Scissors game interface!

## Testing the API

### Option 1: Using the .http File (Recommended)

Open `backend/api-tests.http` in your IDE and run the requests directly.

### Option 2: Using cURL

Play a game with ROCK:
```bash
curl -X POST http://localhost:8080/api/game/play \
  -H "Content-Type: application/json" \
  -d '{"playerHand": "ROCK"}'
```

Example response:
```json
{
  "gameId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "playerHand": "ROCK",
  "computerHand": "SCISSORS",
  "result": "WIN",
  "timestamp": "2025-12-10T20:00:00Z"
}
```

### Option 3: Using Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

## Available Endpoints

### Game API

- POST **/api/game/play** - Play a game of Rock Paper Scissors

### Monitoring & Actuator

- GET **/actuator/health** - Health check
- GET **/actuator/prometheus** - Prometheus metrics


## Metrics & Monitoring

### Prometheus Metrics

Access Prometheus-compatible metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

Key metrics available:
- `http_server_requests_seconds` - Request duration
- `jvm_memory_used_bytes` - JVM memory usage
- `system_cpu_usage` - CPU usage
- Custom game metrics (can be extended)

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```