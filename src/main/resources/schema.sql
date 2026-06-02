DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_waiting;
DROP TABLE IF EXISTS reservation_slot;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE reservation_time (
    id                 BIGINT           NOT NULL AUTO_INCREMENT,
    start_at           TIME             NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE theme (
    id                 BIGINT           NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255)     NOT NULL UNIQUE,
    description        VARCHAR(255)     NOT NULL,
    thumbnail_url      VARCHAR(255)     NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255)    NOT NULL,
    reservation_date   DATE            NOT NULL,
    time_id            BIGINT          NOT NULL,
    theme_id           BIGINT          NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (reservation_date, time_id, theme_id)
);

CREATE TABLE reservation_waiting (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255) NOT NULL,
    reservation_date   DATE         NOT NULL,
    time_id            BIGINT       NOT NULL,
    theme_id           BIGINT       NOT NULL,
    deleted_at         TIMESTAMP    NOT NULL DEFAULT '1970-01-01 00:00:00',
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (reservation_date, time_id, theme_id, name, deleted_at)
);
