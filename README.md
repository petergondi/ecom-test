# ecom-test# 🛒 Demo E-Commerce API

A Spring Boot REST API with JWT authentication, cart management, order checkout, and SMS notifications via Africa's Talking.

---

## Prerequisites

Make sure you have the following installed before running the project:

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com/downloads |
| Git | Any | https://git-scm.com |

Optional but recommended:
- **IntelliJ IDEA** (with Lombok plugin installed)
- **Postman** for testing endpoints
- **Africa's Talking account** for SMS (sandbox is free)

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/          # SecurityConfig
│   │   ├── controller/      # REST controllers
│   │   ├── enums/           # OrderStatus
│   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   ├── models/
│   │   │   └── dtos/        # Request/Response DTOs
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # JwtService, JwtAuthFilter, UserDetailsServiceImpl
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/    # Flyway SQL migrations
│       └── application.properties
```

---

## Setup & Running

### 1. Clone the repository

```bash
git clone https://github.com/petergondi/ecom-test
cd demo
```

### 2. Create the MySQL database

Log into MySQL and create the database:

```sql
CREATE DATABASE your_db_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

jwt.secret=7Rn2pLmKqXvZwYsUhTgFdCbNjEiOaP4xQ8yW1eM6uAoSlDkHvBtJzRcGfInPw9
jwt.expiration=86400000

africastalking.username=sandbox
africastalking.api-key=your_sandbox_api_key
```

### 4. Add Flyway dependency to pom.xml

Inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

### 5. Place migration file

Make sure the migration SQL file is at:

```
src/main/resources/db/migration/V1__init_schema_and_seed_products.sql
```

Flyway runs this automatically on startup — creates all tables and seeds 10 products.

### 6. Enable annotation processing (IntelliJ)

```
Settings → Build, Execution, Deployment → Compiler → Annotation Processors
→ ✅ Enable annotation processing → Apply → OK
```

### 7. Run the application

```bash
./mvnw clean spring-boot:run
```

Or in IntelliJ: open `DemoApplication.java` → click the green ▶ Run button.

The API will start at: **http://localhost:8080**

---

## Verify it's running

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{
  "status": "ok",
  "timestamp": "2026-03-13T08:30:00Z"
}
```

---

## API Endpoints

### 🔓 Auth (public)

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login → get JWT token |
| `GET` | `/api/auth/me` | Get authenticated profile |

### 📦 Products (protected)

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/products?page=1&limit=10` | Paginated product list |
| `GET` | `/api/products/{id}` | Single product (404 if not found) |
| `POST` | `/api/products` | Create product |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Delete product |

### 🛒 Cart (protected)

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/cart` | Get authenticated user's cart |
| `POST` | `/api/cart` | Add item (merges if already in cart) |
| `PATCH` | `/api/cart/{itemId}` | Update qty — send `0` to remove |
| `DELETE` | `/api/cart` | Clear entire cart |

### 🧾 Orders (protected)

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/orders/checkout` | Checkout cart → create order |
| `GET` | `/api/orders` | Order history (newest first) |
| `GET` | `/api/orders/{id}` | Single order with full details |

### 💚 Health (public)

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/health` | Health check |

---

## Authentication

All protected endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

**Flow:**
1. `POST /api/auth/register` — create account
2. `POST /api/auth/login` — get token
3. Include token in all subsequent requests

---
Here is the **raw Markdown**, clean and ready to paste into your README:

---

## 🔐 Auth Examples

### **1. Register New User**

**POST**
`http://localhost:8080/api/auth/register`

**Request Body**

```json
{
    "name": "John Doe",
    "email": "john2@example.com",
    "password": "password123",
    "mobile": "0712345675"
}
```

---

### **2. Login**

**POST**
`http://localhost:8080/api/auth/login`

**Request Body**

```json
{
    "email": "john1@example.com",
    "password": "password123"
}
```

**Example Response**

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huMUBleGFtcGxlLmNvbSIsImlhdCI6MTc3MzQwMDYzOSwiZXhwIjoxNzczNDg3MDM5fQ.kWpxEQ30TWxasoWBN8aukTy7w294gg5j9VbXlexDcGI",
    "type": "Bearer",
    "id": 2,
    "name": "John Doe",
    "email": "john1@example.com"
}
```

Use the token in the header:

```
Authorization: Bearer <token>
```

---

### **3. Get Authenticated User**

**GET**
`http://localhost:8080/api/auth/me`

**Headers**

```
Authorization: Bearer <your_jwt_token>
```

---



## Error Responses

| Status | When |
|--------|------|
| `400` | Missing/invalid fields, empty cart, quantity exceeds stock |
| `401` | Wrong email or password |
| `404` | Resource not found or belongs to another user |
| `409` | Duplicate email/mobile, or out-of-stock product at checkout |

### Example 409 — stock conflict at checkout

```json
{
  "error": "Checkout failed due to insufficient stock",
  "details": [
    "'Sony WH-1000XM5' — requested: 2, available: 0",
    "'The North Face Puffer Jacket' — requested: 1, available: 0"
  ]
}
```

---

## Idempotent Checkout

Sending the same `idempotencyKey` within 10 seconds returns the existing order instead of creating a duplicate:

```json
POST /api/orders/checkout
{
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## SMS Notifications (Africa's Talking)

SMS is sent automatically on:
- ✅ Adding an item to cart
- ✅ Successful checkout (order confirmation)

SMS failures are **silent** — they log the error but never break the API response.

### Setup

1. Sign up free at https://africastalking.com
2. Go to **Sandbox → Settings** → copy your API key
3. Add to `application.properties`:

```properties
africastalking.username=sandbox
africastalking.api-key=your_key_here
```

4. Install the **AT Simulator** from your dashboard to receive test SMS messages

---

## Seeded Products

The migration seeds 10 products across 3 categories. Two are out of stock:

| # | Name | Category | Price (KES) | Stock |
|---|------|----------|-------------|-------|
| 1 | iPhone 15 Pro | Electronics | 164,999 | 25 |
| 2 | Samsung Galaxy S24 Ultra | Electronics | 149,999 | 18 |
| 3 | Sony WH-1000XM5 | Electronics | 34,999 | **0** |
| 4 | Apple MacBook Air M3 | Electronics | 184,999 | 10 |
| 5 | Nike Air Max 270 | Clothing | 12,999 | 40 |
| 6 | Levi's 501 Original Jeans | Clothing | 6,999 | 55 |
| 7 | The North Face Puffer Jacket | Clothing | 18,999 | **0** |
| 8 | Instant Pot Duo 7-in-1 | Home & Kitchen | 8,999 | 30 |
| 9 | Dyson V15 Detect | Home & Kitchen | 54,999 | 12 |
| 10 | Ninja Air Fryer Pro | Home & Kitchen | 7,499 | 22 |

---

## Production Checklist

- [ ] Replace `jwt.secret` with a secure generated value: `openssl rand -base64 32`
- [ ] Set `spring.jpa.show-sql=false`
- [ ] Use environment variables for all secrets
- [ ] Switch Africa's Talking from sandbox to live credentials
- [ ] Change `spring.jpa.hibernate.ddl-auto` to `validate`

```properties
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
africastalking.api-key=${AT_API_KEY}
```

---

## Common Issues
