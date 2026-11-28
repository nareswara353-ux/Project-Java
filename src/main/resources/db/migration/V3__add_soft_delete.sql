ALTER TABLE products ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX idx_products_deleted ON products(deleted);
CREATE INDEX idx_products_deleted_at ON products(deleted_at) WHERE deleted = true;
