CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       name          VARCHAR(100)   NOT NULL,
                       email         VARCHAR(150)   NOT NULL UNIQUE,
                       cash_balance  NUMERIC(18, 2) NOT NULL DEFAULT 0
);

CREATE TABLE orders (
                        id             BIGSERIAL PRIMARY KEY,
                        user_id        BIGINT         NOT NULL REFERENCES users (id),
                        instrument_id  BIGINT         NOT NULL REFERENCES instruments (id),
                        side           VARCHAR(4)     NOT NULL,
                        quantity       INTEGER        NOT NULL CHECK (quantity > 0),
                        price          NUMERIC(18, 2) NOT NULL,
                        status         VARCHAR(16)    NOT NULL,
                        created_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);

INSERT INTO users (name, email, cash_balance) VALUES
                                                  ('Rishabh', 'rishabh@example.com', 500000.00),
                                                  ('Test Trader', 'trader@example.com', 250000.00);