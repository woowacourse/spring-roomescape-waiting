DROP TABLE IF EXISTS waiting;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255),
    description   VARCHAR(500),
    PRIMARY KEY (id)
);


CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
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
    CONSTRAINT uk_reservation_slot UNIQUE (date, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE RESTRICT,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE RESTRICT
);

CREATE TABLE waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_waiting_slot_name UNIQUE (date, time_id, theme_id, name),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE RESTRICT,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE RESTRICT
);

CREATE INDEX idx_reservation_name ON reservation (name);
CREATE INDEX idx_waiting_name_slot ON waiting (name, date, time_id, theme_id);
