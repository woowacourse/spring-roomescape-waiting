DROP TABLE IF EXISTS waiting;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;

CREATE TABLE reservation_time
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    start_at  TIME   NOT NULL,
    finish_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE waiting
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    theme_id BIGINT,
    time_id  BIGINT,
    date     DATE         NOT NULL,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id)
);
