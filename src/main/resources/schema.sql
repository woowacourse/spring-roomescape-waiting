DROP TABLE IF EXISTS store_managers;
DROP TABLE IF EXISTS payment_order;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(127) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    description         TEXT         NOT NULL,
    thumbnail_image_url VARCHAR(512) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    user_id  BIGINT NOT NULL,
    theme_id BIGINT  NOT NULL,
    date     DATE   NOT NULL,
    time_id  BIGINT  NOT NULL,
    store_id BIGINT NOT NULL,
    status   VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE payment_order
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT      NOT NULL,
    order_id       VARCHAR(64) NOT NULL,
    amount         BIGINT      NOT NULL,
    payment_key    VARCHAR(200),
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (order_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE TABLE store_managers
(
    store_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (store_id, user_id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

ALTER TABLE reservation
    ADD CONSTRAINT unique_reservation UNIQUE (date, time_id, theme_id, store_id, user_id);
