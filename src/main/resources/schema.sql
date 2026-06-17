DROP TABLE IF EXISTS store_managers;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_slot;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(127) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE store
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    description         TEXT         NOT NULL,
    thumbnail_image_url VARCHAR(512) NOT NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    start_at   TIME     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_slot
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    date       DATE     NOT NULL,
    theme_id   BIGINT   NOT NULL,
    time_id    BIGINT   NOT NULL,
    store_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    UNIQUE (date, theme_id, time_id, store_id)
);

CREATE TABLE reservation
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    slot_id          BIGINT      NOT NULL,
    status           VARCHAR(30) NOT NULL,
    reserved_slot_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'RESERVED' THEN slot_id END),
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT unique_reservation UNIQUE (slot_id, user_id),
    CONSTRAINT unique_reserved_per_slot UNIQUE (reserved_slot_id)
);

CREATE INDEX idx_reservation_user_id ON reservation (user_id);

CREATE TABLE orders
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(64) NOT NULL,
    reservation_id BIGINT      NOT NULL,
    amount         BIGINT      NOT NULL,
    status         VARCHAR(30) NOT NULL,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT unique_order_id UNIQUE (order_id),
);

CREATE TABLE store_managers
(
    store_id   BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_id, user_id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
