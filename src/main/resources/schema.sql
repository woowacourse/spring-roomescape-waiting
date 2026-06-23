DROP TABLE IF EXISTS reservation_waiting;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;

CREATE TABLE reservation_time
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    start_at  TIME   NOT NULL,
    finish_at TIME   NOT NULL,
    UNIQUE(start_at, finish_at),
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    price       INT          NOT NULL,
    UNIQUE(name),
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT,
    theme_id BIGINT,
    UNIQUE(date, time_id, theme_id),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation_waiting
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT,
    theme_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, date, time_id, theme_id),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
