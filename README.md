# ecom-test — Demo E-Commerce API

A Spring Boot REST API with JWT authentication, cart management, order checkout, and SMS notifications via Africa's Talking.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://interstellar-sunset-5393.postman.co/workspace/My-Workspace~92bac59b-955b-47ba-88ed-fd49b310c73b/collection/4932219-f3c033af-ccdb-4e6a-b194-80f6e18cb93e?action=share&source=copy-link&creator=4932219)

> **Postman Collection** — [Open in Postman](https://interstellar-sunset-5393.postman.co/workspace/My-Workspace~92bac59b-955b-47ba-88ed-fd49b310c73b/collection/4932219-f3c033af-ccdb-4e6a-b194-80f6e18cb93e?action=share&source=copy-link&creator=4932219)

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

```xml
<dependency>
 <groupId>org.flywaydb</groupId>
 <artifactId>flyway-mysql</artifactId>
</dependency>
```

### 5. Place migration file

```
src/main/resources/db/migration/V1__init_schema_and_seed_products.sql
```

Flyway runs this automatically on startup — creates all tables and seeds 10 products.

### 6. Enable annotation processing (IntelliJ)

```
Settings → Build, Execution, Deployment → Compiler → Annotation Processors
→ Enable annotation processing → Apply → OK
```

### 7. Run the application

```bash
./mvnw clean spring-boot:run
```

Or in IntelliJ: open `DemoApplication.java` → click the green Run button.

The API starts at: **http://localhost:8080**

---

## Verify it's running

```bash
curl http://localhost:8080/health
```

```json
{
 "status": "ok",
 "timestamp": "2026-03-13T08:30:00Z"
}
```

---

## Authentication

All protected endpoints require:

```
Authorization: Bearer <your_jwt_token>
```

**Flow:**
1. `POST /api/auth/register` — create account
2. `POST /api/auth/login` — get token
3. Include token in every subsequent request header

---

## API Reference

---

### Auth Endpoints (public)

---

#### POST `/api/auth/register` — Register new user

**Request**
```json
{
 "name": "John Doe",
 "email": "john@example.com",
 "mobile": "0712345678",
 "password": "password123"
}
```

**201 Created**
```json
{
 "token": "eyJhbGciOiJIUzI1NiJ9...",
 "type": "Bearer",
 "id": 1,
 "name": "John Doe",
 "email": "john@example.com"
}
```

**400 — Missing fields**
```json
{
 "errors": {
 "email": "Email is required",
 "password": "Password is required"
 }
}
```

**409 — Email already exists**
```json
{
 "error": "Email already in use: john@example.com"
}
```

---

#### POST `/api/auth/login` — Login and get token

**Request**
```json
{
 "email": "john@example.com",
 "password": "password123"
}
```

**200 OK**
```json
{
 "token": "eyJhbGciOiJIUzI1NiJ9...",
 "type": "Bearer",
 "id": 1,
 "name": "John Doe",
 "email": "john@example.com"
}
```

**401 — Wrong credentials**
```json
{
 "error": "Invalid email or password"
}
```

---

#### GET `/api/auth/me` — Get authenticated profile

**Headers**
```
Authorization: Bearer <token>
```

**200 OK**
```json
{
 "type": "Bearer",
 "id": 1,
 "name": "John Doe",
 "email": "john@example.com"
}
```

---

### Products Endpoints (protected)

---

#### GET `/api/products?page=1&limit=10` — Paginated product list

**Headers**
```
Authorization: Bearer <token>
```

**Query Params**

| Param | Default | Max | Description |
|-------|---------|-----|-------------|
| `page` | `1` | — | Page number |
| `limit` | `10` | `50` | Items per page |

**200 OK**
```json
{
 "data": [
 {
 "id": 1,
 "name": "iPhone 15 Pro",
 "description": "Apple iPhone 15 Pro 256GB",
 "price": 164999.00,
 "category": "Electronics",
 "stock": 25,
 "inStock": true,
 "createdAt": "2026-03-13T08:00:00"
 },
 {
 "id": 3,
 "name": "Sony WH-1000XM5",
 "description": "Wireless Noise-Cancelling Headphones",
 "price": 34999.00,
 "category": "Electronics",
 "stock": 0,
 "inStock": false,
 "createdAt": "2026-03-13T08:00:00"
 }
 ],
 "meta": {
 "total": 10,
 "page": 1,
 "limit": 10,
 "totalPages": 1
 }
}
```

---

#### GET `/api/products/{id}` — Get single product

**200 OK**
```json
{
 "id": 1,
 "name": "iPhone 15 Pro",
 "description": "Apple iPhone 15 Pro 256GB",
 "price": 164999.00,
 "category": "Electronics",
 "stock": 25,
 "inStock": true,
 "createdAt": "2026-03-13T08:00:00"
}
```

**404 — Not found**
```json
{
 "error": "Product not found with id: 99"
}
```

---

#### POST `/api/products` — Create product

**Request**
```json
{
 "name": "iPhone 15 Pro",
 "description": "Apple iPhone 15 Pro 256GB, Titanium finish",
 "price": 164999.00,
 "category": "Electronics",
 "stock": 25
}
```

**201 Created**
```json
{
 "id": 1,
 "name": "iPhone 15 Pro",
 "description": "Apple iPhone 15 Pro 256GB, Titanium finish",
 "price": 164999.00,
 "category": "Electronics",
 "stock": 25,
 "inStock": true,
 "createdAt": "2026-03-13T08:00:00"
}
```

---

#### PUT `/api/products/{id}` — Update product

**Request**
```json
{
 "name": "iPhone 15 Pro",
 "description": "Updated description",
 "price": 159999.00,
 "category": "Electronics",
 "stock": 20
}
```

**200 OK** — returns updated product

---

#### DELETE `/api/products/{id}` — Delete product

**204 No Content** — no body returned

**404 — Not found**
```json
{
 "error": "Product not found with id: 99"
}
```

---

### Cart Endpoints (protected)

---

#### GET `/api/cart` — Get user's cart

**200 OK**
```json
{
 "items": [
 {
 "id": 1,
 "product": {
 "id": 1,
 "name": "iPhone 15 Pro",
 "price": 164999.00,
 "category": "Electronics",
 "stock": 25,
 "inStock": true
 },
 "quantity": 2,
 "lineTotal": 329998.00
 },
 {
 "id": 2,
 "product": {
 "id": 8,
 "name": "Instant Pot Duo 7-in-1",
 "price": 8999.00,
 "category": "Home & Kitchen",
 "stock": 30,
 "inStock": true
 },
 "quantity": 1,
 "lineTotal": 8999.00
 }
 ],
 "cartTotal": 338997.00
}
```

---

#### POST `/api/cart` — Add item to cart

> If the product is already in the cart, quantity is increased automatically.

**Request**
```json
{
 "productId": 1,
 "quantity": 2
}
```

**201 Created** — returns full updated cart (same shape as GET /api/cart)

**400 — Out of stock**
```json
{
 "error": "Product 'Sony WH-1000XM5' is out of stock"
}
```

**400 — Quantity exceeds stock**
```json
{
 "error": "Requested quantity (30) exceeds available stock (25)"
}
```

---

#### PATCH `/api/cart/{itemId}` — Update item quantity

> Send `quantity: 0` to remove the item from the cart.

**Request**
```json
{
 "quantity": 3
}
```

**200 OK** — returns full updated cart

**Request to remove item**
```json
{
 "quantity": 0
}
```

**200 OK** — item removed, returns updated cart

**404 — Item not found or belongs to another user**
```json
{
 "error": "Cart item not found or does not belong to you"
}
```

---

#### DELETE `/api/cart` — Clear entire cart

**204 No Content** — cart cleared, no body returned

**404 — No cart found**
```json
{
 "error": "No cart found for this user"
}
```

---

### Order Endpoints (protected)

---

#### POST `/api/orders/checkout` — Checkout cart

> Atomic — either everything succeeds or nothing changes.
> Unit prices are locked at time of purchase.

**Request** *(body optional)*
```json
{
 "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000"
}
```

**201 Created**
```json
{
 "id": 1,
 "status": "PENDING",
 "totalAmount": 338997.00,
 "createdAt": "2026-03-13T11:30:00",
 "items": [
 {
 "id": 1,
 "productName": "iPhone 15 Pro",
 "productCategory": "Electronics",
 "quantity": 2,
 "unitPrice": 164999.00,
 "lineTotal": 329998.00
 },
 {
 "id": 2,
 "productName": "Instant Pot Duo 7-in-1",
 "productCategory": "Home & Kitchen",
 "quantity": 1,
 "unitPrice": 8999.00,
 "lineTotal": 8999.00
 }
 ]
}
```

**400 — Empty cart**
```json
{
 "error": "Your cart is empty"
}
```

**409 — Stock conflict**
```json
{
 "error": "Checkout failed due to insufficient stock",
 "details": [
 "'Sony WH-1000XM5' — requested: 2, available: 0",
 "'The North Face Puffer Jacket' — requested: 1, available: 0"
 ]
}
```

> **Idempotency:** Sending the same `idempotencyKey` within 10 seconds returns the existing order instead of creating a duplicate.

---

#### GET `/api/orders` — Order history

> Returns authenticated user's orders, newest first.

**200 OK**
```json
[
 {
 "id": 2,
 "status": "PENDING",
 "totalAmount": 12999.00,
 "createdAt": "2026-03-13T12:00:00",
 "items": [
 {
 "id": 3,
 "productName": "Nike Air Max 270",
 "productCategory": "Clothing",
 "quantity": 1,
 "unitPrice": 12999.00,
 "lineTotal": 12999.00
 }
 ]
 },
 {
 "id": 1,
 "status": "PENDING",
 "totalAmount": 338997.00,
 "createdAt": "2026-03-13T11:30:00",
 "items": [
 {
 "id": 1,
 "productName": "iPhone 15 Pro",
 "productCategory": "Electronics",
 "quantity": 2,
 "unitPrice": 164999.00,
 "lineTotal": 329998.00
 },
 {
 "id": 2,
 "productName": "Instant Pot Duo 7-in-1",
 "productCategory": "Home & Kitchen",
 "quantity": 1,
 "unitPrice": 8999.00,
 "lineTotal": 8999.00
 }
 ]
 }
]
```

---

#### GET `/api/orders/{id}` — Get single order

**200 OK**
```json
{
 "id": 1,
 "status": "PENDING",
 "totalAmount": 338997.00,
 "createdAt": "2026-03-13T11:30:00",
 "items": [
 {
 "id": 1,
 "productName": "iPhone 15 Pro",
 "productCategory": "Electronics",
 "quantity": 2,
 "unitPrice": 164999.00,
 "lineTotal": 329998.00
 }
 ]
}
```

**404 — Not found or belongs to another user**
```json
{
 "error": "Order not found or does not belong to you"
}
```

---

### Health (public)

#### GET `/health`

**200 OK**
```json
{
 "status": "ok",
 "timestamp": "2026-03-13T08:30:00Z"
}
```

---

## Error Responses Summary

| Status | When |
|--------|------|
| `400` | Missing/invalid fields, empty cart, quantity exceeds stock |
| `401` | Wrong email or password, missing/expired token |
| `404` | Resource not found or belongs to another user |
| `409` | Duplicate email/mobile, out-of-stock product at checkout |

---

## SMS Notifications (Africa's Talking)

SMS is sent automatically on:
- Adding an item to cart (cart summary)
- Successful checkout (order confirmation)

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

- [ ] Replace `jwt.secret` with a secure value: `openssl rand -base64 32`
- [ ] Set `spring.jpa.show-sql=false`
- [ ] Use environment variables for all secrets
- [ ] Switch Africa's Talking from sandbox to live credentials
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`

```properties
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
africastalking.api-key=${AT_API_KEY}
```

---

## Common Issues

| Issue | Fix |
|-------|-----|
| `Cannot load driver class: com.mysql.cj.jdbc.Driver` | Run `./mvnw dependency:resolve` then reload Maven |
| `getEmail() / builder() not found` | Enable annotation processing in IntelliJ + **Build → Rebuild Project** |
| `Port 8080 already in use` | Run `lsof -ti:8080 \| xargs kill -9` |
| Flyway migration fails | Drop and recreate the DB, then restart |
| `jwt.secret` weak key error | Generate: `openssl rand -base64 32` |
| `missing column [updated_at] in cart_items` | Run `ALTER TABLE cart_items MODIFY COLUMN updated_at DATETIME(6) NULL;` in MySQL |