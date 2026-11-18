CREATE TABLE instruments (
                             id          BIGSERIAL PRIMARY KEY,
                             symbol      VARCHAR(20)    NOT NULL UNIQUE,
                             name        VARCHAR(150)   NOT NULL,
                             exchange    VARCHAR(20)    NOT NULL,
                             last_price  NUMERIC(18, 2) NOT NULL
);

INSERT INTO instruments (symbol, name, exchange, last_price) VALUES
                                                                 ('RELIANCE', 'Reliance Industries Ltd',   'NSE', 1420.50),
                                                                 ('TCS',      'Tata Consultancy Services', 'NSE', 3180.00),
                                                                 ('INFY',     'Infosys Ltd',               'NSE', 1545.75);