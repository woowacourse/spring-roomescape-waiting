DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_slot;
DROP TABLE IF EXISTS reservation_date;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE reservation_date
(
    id   BIGINT NOT NULL AUTO_INCREMENT,
    date DATE   NOT NULL UNIQUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL UNIQUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(30)  NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    price         BIGINT       NOT NULL DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date_id  BIGINT NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE (date_id, time_id, theme_id)
);

CREATE INDEX id_slot_time ON reservation_slot (time_id);
CREATE INDEX id_slot_theme ON reservation_slot (theme_id);

CREATE TABLE reservation
(
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    name    VARCHAR(30) NOT NULL,
    slot_id BIGINT      NOT NULL,
    status  VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    PRIMARY KEY (id),
    UNIQUE (slot_id, name)
);

CREATE INDEX idx_reservation_name ON reservation (name);

CREATE TABLE payment
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT      NOT NULL UNIQUE,
    order_id       VARCHAR(64) NOT NULL UNIQUE,
    amount         BIGINT      NOT NULL,
    payment_key    VARCHAR(200),
    PRIMARY KEY (id)
);
