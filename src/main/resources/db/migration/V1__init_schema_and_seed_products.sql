-- ============================================================
-- Migration: V1__init_schema_and_seed_products.sql
-- Place in: src/main/resources/db/migration/
-- ============================================================


-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
                                     id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name         VARCHAR(100)        NOT NULL,
    email        VARCHAR(150)        NOT NULL UNIQUE,
    mobile       VARCHAR(20)         NOT NULL UNIQUE,
    password_hash VARCHAR(255)       NOT NULL,
    created_at   DATETIME(6)         NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    );

-- ── Products ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
                                        id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name        VARCHAR(150)        NOT NULL,
    description TEXT,
    price       DECIMAL(12, 2)      NOT NULL,
    category    VARCHAR(100)        NOT NULL,
    stock       INT                 NOT NULL DEFAULT 0,
    created_at  DATETIME(6)         NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    );

-- ── Orders ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
                                      id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id           BIGINT          NOT NULL,
                                      total_amount      DECIMAL(12, 2)  NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    idempotency_key   VARCHAR(255)    UNIQUE,
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
    );

-- ── Order Items ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
                                           id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           order_id    BIGINT          NOT NULL,
                                           product_id  BIGINT          NOT NULL,
                                           quantity    INT             NOT NULL,
                                           unit_price  DECIMAL(12, 2)  NOT NULL,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
    );

-- ── Cart Items ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cart_items (
                                          id          INT AUTO_INCREMENT PRIMARY KEY,
                                          user_id     BIGINT  NOT NULL,
                                          product_id  BIGINT  NOT NULL,
                                          quantity    INT     NOT NULL,
                                          added_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products (id)
    );

-- ============================================================
-- Seed: 10 products across 3 categories
-- 2 products have stock = 0 (out of stock)
-- ============================================================

INSERT INTO products (name, description, price, category, stock) VALUES

-- ── Electronics (4 products) ──────────────────────────────────────────────────
('iPhone 15 Pro',
 'Apple iPhone 15 Pro 256GB, Titanium finish, A17 Pro chip',
 164999.00, 'Electronics', 25),

('Samsung Galaxy S24 Ultra',
 'Samsung Galaxy S24 Ultra 512GB, Snapdragon 8 Gen 3, 200MP camera',
 149999.00, 'Electronics', 18),

('Sony WH-1000XM5',
 'Sony WH-1000XM5 Wireless Noise-Cancelling Headphones',
 34999.00, 'Electronics', 0),   -- OUT OF STOCK

('Apple MacBook Air M3',
 'Apple MacBook Air 13-inch M3 chip, 16GB RAM, 512GB SSD',
 184999.00, 'Electronics', 10),

-- ── Clothing (3 products) ─────────────────────────────────────────────────────
('Nike Air Max 270',
 'Nike Air Max 270 Running Shoes, Lightweight and breathable',
 12999.00, 'Clothing', 40),

('Levi\'s 501 Original Jeans',
 'Levi\'s 501 Original Fit Jeans, Classic straight leg, 100% cotton',
 6999.00, 'Clothing', 55),

('The North Face Puffer Jacket',
 'The North Face 700-fill Down Puffer Jacket, Water-resistant',
 18999.00, 'Clothing', 0),      -- OUT OF STOCK

-- ── Home & Kitchen (3 products) ───────────────────────────────────────────────
('Instant Pot Duo 7-in-1',
 'Instant Pot Duo 7-in-1 Electric Pressure Cooker, 6 Quart',
 8999.00, 'Home & Kitchen', 30),

('Dyson V15 Detect',
 'Dyson V15 Detect Cordless Vacuum Cleaner with laser dust detection',
 54999.00, 'Home & Kitchen', 12),

('Ninja Air Fryer Pro',
 'Ninja Air Fryer Pro 5.5L, 1550W, non-stick basket',
 7499.00, 'Home & Kitchen', 22);