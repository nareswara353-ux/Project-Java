CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    stock INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_products_name UNIQUE (name)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    details TEXT,
    actor VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_product_id ON audit_log(product_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor);
