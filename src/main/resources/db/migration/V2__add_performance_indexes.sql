CREATE INDEX idx_products_price_stock ON products(price, stock);
CREATE INDEX idx_products_name_lower ON products(LOWER(name));
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_product_timestamp ON audit_log(product_id, timestamp DESC);
CREATE INDEX idx_audit_log_timestamp_actor ON audit_log(timestamp DESC, actor);
