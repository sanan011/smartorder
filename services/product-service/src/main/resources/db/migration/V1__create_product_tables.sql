-- ============================================================
-- V1: Product Service Schema
-- ============================================================

CREATE TABLE categories (
                            id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                            name            VARCHAR(150)    NOT NULL,
                            slug            VARCHAR(200)    NOT NULL,
                            description     TEXT,
                            parent_id       UUID,
                            display_order   INTEGER         NOT NULL DEFAULT 0,
                            active          BOOLEAN         NOT NULL DEFAULT TRUE,
                            created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                            updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                            created_by      VARCHAR(100),
                            updated_by      VARCHAR(100),

                            CONSTRAINT pk_categories PRIMARY KEY (id),
                            CONSTRAINT uq_categories_slug UNIQUE (slug),
                            CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
                                REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active    ON categories(active);

CREATE TABLE products (
                          id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
                          name                VARCHAR(255)    NOT NULL,
                          description         TEXT,
                          slug                VARCHAR(300)    NOT NULL,
                          price               NUMERIC(19,2)   NOT NULL,
                          currency_code       VARCHAR(3)      NOT NULL DEFAULT 'USD',
                          compare_at_price    NUMERIC(19,2),
                          category_id         UUID            NOT NULL,
                          seller_id           UUID            NOT NULL,
                          status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
                          rejection_reason    TEXT,
                          sku                 VARCHAR(100),
                          brand               VARCHAR(150),
                          average_rating      DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                          review_count        INTEGER          NOT NULL DEFAULT 0,
                          tags                TEXT,
                          created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                          updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                          created_by          VARCHAR(100),
                          updated_by          VARCHAR(100),

                          CONSTRAINT pk_products         PRIMARY KEY (id),
                          CONSTRAINT uq_products_slug    UNIQUE (slug),
                          CONSTRAINT fk_products_category FOREIGN KEY (category_id)
                              REFERENCES categories(id),
                          CONSTRAINT chk_products_price  CHECK (price >= 0),
                          CONSTRAINT chk_products_status CHECK (status IN (
                                                                           'DRAFT','PENDING','ACTIVE','INACTIVE','REJECTED','DELETED'
                              ))
);

CREATE INDEX idx_products_seller_id   ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status      ON products(status);
CREATE INDEX idx_products_slug        ON products(slug);
CREATE UNIQUE INDEX idx_products_sku_seller ON products(sku, seller_id)
    WHERE sku IS NOT NULL;

CREATE TABLE product_images (
                                id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                                product_id      UUID            NOT NULL,
                                object_key      VARCHAR(500)    NOT NULL,
                                url             VARCHAR(1000)   NOT NULL,
                                alt_text        VARCHAR(255),
                                display_order   INTEGER         NOT NULL DEFAULT 0,
                                primary_image   BOOLEAN         NOT NULL DEFAULT FALSE,

                                CONSTRAINT pk_product_images    PRIMARY KEY (id),
                                CONSTRAINT fk_images_product    FOREIGN KEY (product_id)
                                    REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);

-- Seed categories
INSERT INTO categories (id, name, slug, display_order, created_by, updated_by)
VALUES
    (gen_random_uuid(), 'Electronics',  'electronics',  1, 'SYSTEM', 'SYSTEM'),
    (gen_random_uuid(), 'Clothing',     'clothing',     2, 'SYSTEM', 'SYSTEM'),
    (gen_random_uuid(), 'Home & Garden','home-garden',  3, 'SYSTEM', 'SYSTEM'),
    (gen_random_uuid(), 'Sports',       'sports',       4, 'SYSTEM', 'SYSTEM'),
    (gen_random_uuid(), 'Books',        'books',        5, 'SYSTEM', 'SYSTEM');

-- Auto-update trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();