DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme_slot;
DROP TABLE IF EXISTS time;
DROP TABLE IF EXISTS theme;

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL,
    price         BIGINT       NOT NULL DEFAULT 10000,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme_slot
(
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    theme_id    BIGINT  NOT NULL,
    date        DATE    NOT NULL,
    time_id     BIGINT  NOT NULL,
    is_reserved BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (theme_id, date, time_id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    theme_slot_id BIGINT       NOT NULL,
    order_id      VARCHAR(64)  NULL,
    amount        BIGINT       NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_slot_id) REFERENCES theme_slot (id)
);

CREATE TABLE IF NOT EXISTS payment
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    payment_key    VARCHAR(200) NOT NULL,
    order_id       VARCHAR(64)  NOT NULL,
    amount         BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);
