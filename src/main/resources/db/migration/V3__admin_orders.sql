ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
CREATE INDEX idx_products_active ON products(active);

-- Supprimer un produit le retire aussi des paniers en cours
ALTER TABLE cart_items DROP CONSTRAINT cart_items_product_id_fkey;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_product_id_fkey
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

CREATE TABLE admin_users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(160) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    full_name      VARCHAR(120) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    number        VARCHAR(30)   NOT NULL UNIQUE,
    status        VARCHAR(20)   NOT NULL DEFAULT 'EN_ATTENTE',
    email         VARCHAR(160)  NOT NULL,
    first_name    VARCHAR(80)   NOT NULL,
    last_name     VARCHAR(80)   NOT NULL,
    phone         VARCHAR(40),
    address       VARCHAR(255)  NOT NULL,
    postal_code   VARCHAR(20)   NOT NULL,
    city          VARCHAR(120)  NOT NULL,
    country       VARCHAR(80)   NOT NULL,
    subtotal      NUMERIC(10,2) NOT NULL,
    shipping      NUMERIC(10,2) NOT NULL,
    total         NUMERIC(10,2) NOT NULL,
    note          TEXT,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_status CHECK (status IN ('EN_ATTENTE','PAYEE','EXPEDIEE','LIVREE','ANNULEE'))
);
CREATE INDEX idx_orders_created ON orders(created_at DESC);
CREATE INDEX idx_orders_status  ON orders(status);

CREATE TABLE order_items (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    BIGINT        REFERENCES products(id) ON DELETE SET NULL,
    product_name  VARCHAR(200)  NOT NULL,
    brand_name    VARCHAR(120)  NOT NULL,
    color_name    VARCHAR(60),
    unit_price    NUMERIC(10,2) NOT NULL,
    quantity      INT           NOT NULL CHECK (quantity > 0),
    line_total    NUMERIC(10,2) NOT NULL
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE SEQUENCE order_number_seq START 1001;
