CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_slot
(
    id               BIGINT    NOT NULL AUTO_INCREMENT,
    reservation_date DATE      NOT NULL,
    time_id          BIGINT    NOT NULL,
    theme_id         BIGINT    NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT unique_reservation_slot_date_time_theme UNIQUE (reservation_date, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS waiting
(
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    customer_name       VARCHAR(10) NOT NULL,
    customer_email      VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    slot_id             BIGINT      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT unique_waiting_slot_customer_email UNIQUE (slot_id, customer_email)
);

CREATE INDEX IF NOT EXISTS idx_waiting_customer_name_email
    ON waiting (customer_name, customer_email);

CREATE TABLE IF NOT EXISTS reservation
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    customer_name    VARCHAR(10) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    slot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT unique_reservation_slot UNIQUE (slot_id)
);

CREATE INDEX IF NOT EXISTS idx_reservation_customer_name_email
    ON reservation (customer_name, customer_email);
