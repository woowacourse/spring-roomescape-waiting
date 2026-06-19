DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unique_reservation_time UNIQUE (start_at)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    url         VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT unique_theme UNIQUE (name)
);

CREATE TABLE reservation
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    date          DATE         NOT NULL,
    time_id       BIGINT,
    theme_id      BIGINT,
    status        VARCHAR(255) NOT NULL,
    order_id      VARCHAR(255),
    payment_key   VARCHAR(255),
    amount        BIGINT,

    confirmed_key INT INVISIBLE GENERATED ALWAYS AS (CASE WHEN status = 'CONFIRMED' THEN 1 END),

    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT unique_reservation UNIQUE (name, date, time_id, theme_id),
    CONSTRAINT unique_confirmed_reservation UNIQUE (date, time_id, theme_id, confirmed_key)
);
