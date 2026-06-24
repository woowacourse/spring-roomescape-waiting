DROP TABLE IF EXISTS waiting;
DROP TABLE IF EXISTS reservation_payment;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;

CREATE TABLE reservation_time
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    start_at  TIME   NOT NULL,
    finish_at TIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unique_time UNIQUE (start_at, finish_at)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT unique_reservation UNIQUE (date, time_id, theme_id)
);

CREATE TABLE waiting
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    date     DATE         NOT NULL,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT unique_waiting UNIQUE (date, time_id, theme_id, name)
);

CREATE TABLE reservation_payment
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT       NOT NULL UNIQUE,
    order_id         VARCHAR(100) NOT NULL UNIQUE,
    payment_key      VARCHAR(200),
    amount           BIGINT       NOT NULL,
    idempotency_key  VARCHAR(300) NOT NULL UNIQUE,
    status           VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE CASCADE
);
