SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS time_slot CASCADE;
DROP TABLE IF EXISTS slot CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS waiting CASCADE;

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE time_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_time_slot_start_at UNIQUE (start_at)
);

CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_slot_date_time_theme UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    slot_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id),
    CONSTRAINT uk_reservation_slot UNIQUE (slot_id)
);

CREATE TABLE waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(250) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    slot_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id),
    CONSTRAINT uk_waiting_name_slot UNIQUE (name, slot_id)
);

TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE slot RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE time_slot RESTART IDENTITY;

SET REFERENTIAL_INTEGRITY TRUE;
