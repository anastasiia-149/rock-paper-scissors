# Rock Paper Scissors Game

A full-stack Rock Paper Scissors game with user authentication, persistent statistics, and comprehensive monitoring.

## Features and technical highlights

-  Play Rock Paper Scissors against a computer opponent
-  Register and login with username
-  Track wins, losses, draws, win rate, and last game played
-  Seamless login experience with automatic statistics loading
-  30 requests per minute per IP to prevent abuse
-  Health checks, Prometheus metrics, and correlation ID tracking
- ️ Database Persistence: PostgreSQL with Flyway migrations
-  Angular 18 with Signals and zoneless change detection
-  Unit and integration tests for both frontend and backend

## Architecture

This project implements **Hexagonal Architecture** (Ports and Adapters) with clean separation of concerns:

### Backend Structure
```
backend/
├── control/                    # Core Business Logic (Domain Layer)
│   ├── model/                  # Domain models
│   │   ├── Game                # Game domain model
│   │   ├── User                # User domain model
│   │   ├── UserStatistics      # Statistics domain model
│   │   ├── Hand                # Hand enum (ROCK, PAPER, SCISSORS)
│   │   └── GameResult          # Result enum (WIN, LOSE, DRAW)
│   ├── ports/                  # Port interfaces (dependencies)
│   │   ├── MetricsProvider     # Metrics tracking port
│   │   ├── RandomHandProvider  # Random hand generation port
│   │   ├── UserRegistrationPort    # User persistence port
│   │   └── UserStatisticsPort  # Statistics persistence port
│   └── exception/              # Domain exceptions
│       └── DomainException     # Custom domain exception
│   ├── GameService             # Game logic service
│   ├── UserRegistrationService # User registration service
│   └──  StatisticsService       # Statistics management service
├── boundary/                   # External Interactions (Adapters)
│   ├── incoming/               # REST controllers (Primary Adapters)
│   │   ├── GameApiController   # HTTP endpoints
│   │   └── GlobalExceptionHandler     # Error handling
│   └── outgoing/               # Infrastructure Adapters (Secondary Adapters)
│       ├── db/                 # Database entities & repositories
│       │   ├── UserEntity      # JPA user entity
│       │   ├── UserRepository  # User repository
│       │   ├── UserStatisticsEntity   # JPA statistics entity
│       │   └── UserStatisticsRepository   # Statistics repository
│       ├── GameAdapter         # Game persistence implementation
│       ├── UserRegistrationAdapter    # User persistence implementation
│       └── UserStatisticsAdapter      # Statistics persistence implementation
└── config/                     # Configuration classes
    ├── RateLimitingConfig      # Rate limiting with Bucket4j
    ├── LoggingConfig           # Correlation ID tracking
    └── CorsConfig              # CORS configuration
```

### Frontend Structure
```
frontend/
├── components/
│   ├── login/        # Login/Registration component
│   └── game/         # Game play component with statistics
├── services/
│   └── game.service.ts    # Game service with Signals (state management)
├── models/
│   └── game.model.ts      # TypeScript interfaces (GameResponse, UserStatistics, etc.)
└── app.config.ts          # Zoneless configuration with routing
```

### Database Schema

```sql
users
  - id (BIGSERIAL PRIMARY KEY)
  - username (VARCHAR(50) UNIQUE)
  - created_at (TIMESTAMP)
  - updated_at (TIMESTAMP)

user_statistics
  - id (BIGSERIAL PRIMARY KEY)
  - user_id (BIGINT, FK to users.id)
  - games_played (INTEGER)
  - wins (INTEGER)
  - losses (INTEGER)
  - draws (INTEGER)
  - last_game_id (VARCHAR(255))
  - last_game_played_at (TIMESTAMP)
  - created_at (TIMESTAMP)
  - updated_at (TIMESTAMP)
```

## Technology Stack

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.3.5**
- **Gradle 8.5**
- **PostgreSQL** (Database)
- **Flyway** (Database migrations)
- **OpenAPI 3.0** (for API-first design + code generation)
- **Micrometer + Prometheus** (Metrics)
- **Spring Actuator** (Health checks & monitoring)
- **SLF4J + Logback** (Logging)
- **Bucket4j** (Rate limiting)

### Frontend
- **Angular 18**
- **TypeScript 5.5**
- **Zoneless Change Detection**
- **Signals** (Reactive state management)

## Prerequisites

- **Java 21** or higher
- **Node.js 18+** and **npm**
- **Gradle 8.5** or higher (wrapper included)
- **PostgreSQL 14+** (Database server)
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse

## Quick Start

### 1. Clone the Repository

```bash
cd ..../test-project
```

### 2. Setup PostgreSQL Database

**Using Docker Compose (Recommended)**

The project includes a ready-to-use Docker Compose configuration:

```bash
cd backend/src/main/docker
docker-compose up -d
```

This will start:
- **PostgreSQL 16.4** on port `5432` (with automatic database initialization)
- **Adminer** (database management tool) on port `8001`

Access Adminer at `http://localhost:8001`:
- System: PostgreSQL
- Server: db
- Username: postgres
- Password: postgres
- Database: rockpaperscissors


Database migrations will run automatically on startup using Flyway.

### 3. Build the Backend

```bash
cd backend
./gradlew clean build
```

This will:
- Generate API interfaces and DTOs from OpenAPI specification
- Compile the application
- Run all tests

### 4. Start the Backend Server

```bash
./gradlew bootRun
```

The server will start on **http://localhost:8080**

Flyway will automatically run database migrations on startup, creating:
- `users` table - stores registered users
- `user_statistics` table - stores game statistics per user

### 5. Start the Frontend

In a new terminal window:

```bash
cd frontend
npm install
npm start
```

The frontend will start on **http://localhost:4200**

### 6. Verify Everything is Running

**Backend health check:**
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  ...
}
```

**Frontend**: Open your browser to **http://localhost:4200**

You should see the Rock Paper Scissors game login page!

## How to Play

1. **Login/Register**: Enter a username (3-50 characters)
   - If the username exists, you'll be logged in with your existing statistics
   - If it's new, a user account will be created for you

2. **Play the Game**: Choose Rock, Paper, or Scissors
   - Your statistics are automatically saved after each game

3. **View Statistics**:
   - See your total games played, wins, losses, draws, and win rate
   - View when you last played a game

4. **Logout**: Click the logout button to switch users

## Testing the API

### Option 1: Using the .http File (Recommended)

Open `backend/api-tests.http` in your IDE and run the requests directly.

### Option 2: Using Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

## Available Endpoints

### Game API

- **POST** `/api/v1/game/user` - Register a new user
- **POST** `/api/v1/game/play` - Play a game of Rock Paper Scissors (requires username)
- **GET** `/api/v1/game/statistics/{username}` - Get user statistics

### Monitoring & Actuator

- **GET** `/actuator/health` - Health check (includes database status)
- **GET** `/actuator/prometheus` - Prometheus metrics

### Rate Limiting

All API endpoints are rate-limited to **30 requests per minute per IP address**. When exceeded, you'll receive a `429 Too Many Requests` response.


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