DROP TABLE IF EXISTS reservation_waiting;
DROP TABLE IF EXISTS reservation_payment;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;

CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255)  NOT NULL,
    description   VARCHAR(255)  NOT NULL,
    thumbnail_url VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (date, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS reservation_waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (name, date, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS reservation_payment
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    order_id        VARCHAR(64)  NOT NULL,
    idempotency_key VARCHAR(300) NOT NULL,
    amount          BIGINT       NOT NULL,
    payment_key     VARCHAR(255),
    payment_status  VARCHAR(30)  NOT NULL,
    failure_code    VARCHAR(255),
    failure_message VARCHAR(1024),
    name            VARCHAR(255) NOT NULL,
    date            DATE         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_id         BIGINT       NOT NULL,
    theme_id        BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (order_id),
    UNIQUE (idempotency_key),
    UNIQUE (date, time_id, theme_id)
);
