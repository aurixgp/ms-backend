-- =========================================================
--  Schéma catalogue + panier
-- =========================================================

CREATE TABLE brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    premium     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    parent_id   BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    position    INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_categories_parent ON categories(parent_id);

CREATE TABLE colors (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    slug        VARCHAR(60)  NOT NULL UNIQUE,
    hex         VARCHAR(7)   NOT NULL
);

CREATE TABLE materials (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    slug        VARCHAR(60)  NOT NULL UNIQUE
);

CREATE TABLE patterns (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    slug        VARCHAR(60)  NOT NULL UNIQUE
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    slug            VARCHAR(200)  NOT NULL UNIQUE,
    name            VARCHAR(200)  NOT NULL,
    description     TEXT,
    brand_id        BIGINT        NOT NULL REFERENCES brands(id),
    category_id     BIGINT        NOT NULL REFERENCES categories(id),
    color_id        BIGINT        NOT NULL REFERENCES colors(id),
    material_id     BIGINT        REFERENCES materials(id),
    pattern_id      BIGINT        REFERENCES patterns(id),
    price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    original_price  NUMERIC(10,2) CHECK (original_price IS NULL OR original_price > price),
    rating          NUMERIC(2,1)  NOT NULL DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    review_count    INT           NOT NULL DEFAULT 0,
    bestseller      BOOLEAN       NOT NULL DEFAULT FALSE,
    is_new          BOOLEAN       NOT NULL DEFAULT FALSE,
    exclusive       BOOLEAN       NOT NULL DEFAULT FALSE,
    stock           INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand    ON products(brand_id);
CREATE INDEX idx_products_color    ON products(color_id);
CREATE INDEX idx_products_price    ON products(price);
CREATE INDEX idx_products_created  ON products(created_at DESC);

CREATE TABLE product_images (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    alt         VARCHAR(255),
    position    INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_product_images_product ON product_images(product_id);

CREATE TABLE carts (
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     UUID   NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id),
    quantity    INT    NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);
