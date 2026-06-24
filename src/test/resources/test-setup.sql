SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS reservation_slot CASCADE;
DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS time_slot CASCADE;

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL,
    price         BIGINT       NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE time_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_time_slot_start_at UNIQUE (start_at)
);

CREATE TABLE reservation_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_slot_date_time_theme UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    slot_id    BIGINT       NOT NULL,
    created_at TIMESTAMP  NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT uk_reservation_name_slot_id UNIQUE (name, slot_id)
);

CREATE INDEX IF NOT EXISTS idx_reservation_name
    ON reservation (name);

CREATE INDEX IF NOT EXISTS idx_reservation_slot_id
    ON reservation (slot_id);

CREATE TABLE orders
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(64) NOT NULL,
    amount         BIGINT      NOT NULL,
    reservation_id BIGINT      NOT NULL,
    status         VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT uk_orders_order_id UNIQUE (order_id)
);

CREATE TABLE payment
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    payment_key VARCHAR(200) NOT NULL,
    order_id    VARCHAR(64)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT uk_payment_payment_key UNIQUE (payment_key),
    CONSTRAINT uk_payment_order_id UNIQUE (order_id)
);

SET REFERENTIAL_INTEGRITY TRUE;
