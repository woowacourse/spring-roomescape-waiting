DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_waiting;
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
    time_id BIGINT                     NOT NULL,
    theme_id BIGINT                    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (reservation_date, theme_id, time_id)
);

CREATE INDEX idx_reservation_name
    ON reservation (name);

CREATE TABLE reservation_waiting (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255) NOT NULL,
    reservation_date   DATE         NOT NULL,
    time_id            BIGINT       NOT NULL,
    theme_id           BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (reservation_date, theme_id, time_id, name)
);

CREATE INDEX idx_reservation_waiting_slot_id
    ON reservation_waiting (reservation_date, theme_id, time_id, id);

CREATE INDEX idx_reservation_waiting_name
    ON reservation_waiting (name);
