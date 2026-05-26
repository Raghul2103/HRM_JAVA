# Construction Workforce HRMS / Overtime Settlement System

This project is a complete enterprise-grade full-stack Construction Workforce Attendance & Overtime Settlement platform. It supports site-based clock-in/out, Redis-based real-time tracking, automatic capped overtime calculations, atomic settlements, transaction-bound SMS alerts, and contains fixes for critical production tickets (**LF-201** through **LF-205**).

---

## 1. Project Attribution & AI Tools Usage

* **Forked Repo**: This project is a customized fork of `spring-boot-realworld-example-app` (chosen because it showcases clean DTO mappings, custom JWT authorization patterns, and repository conventions suitable for production adaptations).
* **AI Tools Used**: Gemini Code Assist and Antigravity.
  * Used for: Generating database schema DDL migrations, boilerplate React component layouts with responsive Tailwind CSS, configuring Redis connection factories with Lettuce, writing JUnit/Mockito mocks, and troubleshooting Tomcat connection starvation bugs.

---

## 2. Getting Started & Setup Instructions

### Prerequisites
- **Java 17+** (JDK 17 or higher)
- **Node.js 20+**
- **Maven 3.8+**
- **Docker & Docker Compose** (optional, for localized Postgres/Redis testing)

---

### Database Setup: Supabase Connection Setup Steps
Follow these steps to connect the application to a cloud PostgreSQL instance hosted on Supabase:

1. **Create a Supabase Project**:
   - Go to [Supabase](https://supabase.com) and sign in.
   - Click **New Project**, choose an organization, set a project name, select a database password, and choose your region.
2. **Obtain Connection String (PgBouncer Pooler)**:
   - Navigate to **Project Settings** (gear icon) -> **Database**.
   - Under the **Connection String** section, select the **URI** tab and choose **Transaction Mode** (uses PgBouncer pooler on port `6543`).
   - The connection string will look like this:
     `postgresql://postgres.[your-project-id]:[your-password]@aws-0-[region].pooler.supabase.com:6543/postgres?pgbouncer=true`
3. **Configure Environment Variables**:
   - Set the following environment variables locally or in your deployment environment:
     ```env
     DB_URL=jdbc:postgresql://postgres.[your-project-id].supabase.co:6543/postgres?pgbouncer=true
     DB_USERNAME=postgres.[your-project-id]
     DB_PASSWORD=your-database-password
     ```
   - Note: Since Supabase kills idle connections, we have pre-configured HikariCP parameters (`max-lifetime: 1800000` (30m) and `keepalive-time: 30000` (30s)) in `application-staging.yml` and `application-prod.yml` to keep connection pools active and warm.

---

### Local Development Quick Start

#### 1. Setup Local Redis
Ensure a local Redis instance is running on `localhost:6379`.
If using Docker, run:
```bash
docker run --name hrm-redis -p 6379:6379 -d redis:alpine
```
*Note: If Redis goes offline, the backend uses a custom `CacheErrorHandler` and automatically degrades gracefully to database queries without crashing endpoints.*

#### 2. Run Backend
On startup, Flyway automatically runs database migrations, and a CommandLineRunner seeds default operators, sites, and workers.
```bash
cd backend
mvn clean package -DskipTests
java -jar target/workforce-hrms-1.0.0.jar
```
Or run the main class `com.hrm.workforce.WorkforceHrmsApplication` inside your IDE.

#### 3. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
The React development server runs on `http://localhost:3000`.

---

## 3. Important Design Decisions

### Caching Choices (Redis Integration)
- **Active Crew Tracking**: Real-time checked-in workers are stored inside a Redis cache with worker and site details. This allows the `/active` endpoint to fetch data with sub-10ms response times.
- **16-Hour TTL Safety Net**: To handle cases where workers or supervisors forget to clock out, each Redis cache entry is configured with a **16-hour Time-to-Live (TTL)**. Once expired, the entry is automatically evicted, preventing stale site logs.
- **Cache Invalidation**: On worker profile changes (name, daily wage rate, designation), the system invokes `activeWorkerCache.evictWorker(id)` to immediately evict stale cached records, ensuring read consistency.

### Schema Tradeoffs
- **Denormalized Hours**: We store calculated fields (`total_hours` and `overtime_hours`) directly on the `attendance_logs` table rather than calculating them at runtime during queries. This reduces CPU overhead on historical log retrieval.
- **Locking Overtime Rates**: Daily wage rates can change over time. If a worker gets a raise, historical overtime payments must remain unaffected. To solve this, the hourly rate applied at the moment of clock-out is copied and locked in the `overtime_entries` table (`overtime_rate_applied`).

### Transaction Boundaries & Decoupled I/O
- **Atomic Batch Settlements**: Overtime settlements are processed as a single atomic unit using `@Transactional(rollbackFor = Exception.class)`. If settling 22 entries fails on entry #15 due to data constraints, the transaction rolls back all updates, preventing partial data leaks.
- **Asynchronous Commit-Bound Notifications**: SMS notifications are sent asynchronously and only **after the database transaction successfully commits**. This is done by publishing an `OvertimeSettledEvent` and handling it via a listener annotated with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- **Decoupled Network I/O**: RestTemplate calls to the external Government minimum-wage API are executed in the controllers *before* opening a database transaction, preventing pool starvation during long network waits.

---

## 4. Things We'd Do Differently With More Time

1. **Persistent Message Queue for Notifications**: Use RabbitMQ or ActiveMQ to handle SMS notification dispatches. If the SMS provider is offline, events are not lost but queued for automatic retries.
2. **Soft Deletions for Sites and Workers**: Implement SQL-level soft deletes (using Hibernate's `@SQLDelete` and `@Where` annotations) instead of manual state flags, preventing foreign key check issues on historical logs.
3. **WebSocket Support**: Implement WebSockets or Server-Sent Events (SSE) alongside the Redis cache to push real-time clock-in and clock-out updates directly to supervisors' screens.

---

## 5. API & cURL Usage Examples

### 1. Authenticate / Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. Clock-In (Supervisor)
```bash
curl -X POST http://localhost:8080/api/attendance/clock-in \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"workerId": 1, "siteId": 1}'
```

### 3. Clock-Out (Supervisor)
```bash
curl -X POST http://localhost:8080/api/attendance/clock-out \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"workerId": 1}'
```

### 5. Get Active Workers (Supervisor / Manager)
```bash
curl -X GET http://localhost:8080/api/attendance/active \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 6. Fetch Attendance History (HR / Admin)
```bash
curl -X GET "http://localhost:8080/api/attendance/log?workerId=1&page=0&size=15" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7. Overtime Summary (Payroll)
```bash
curl -X GET "http://localhost:8080/api/overtime/summary/1?month=2026-04" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 8. Overtime Settlement (Payroll)
```bash
curl -X POST "http://localhost:8080/api/overtime/settle/1?month=2026-04" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```
