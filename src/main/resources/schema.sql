CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(255) NOT NULL,
    description         VARCHAR(255) NOT NULL,
    duration_time       VARCHAR(255) NOT NULL,
    deleted_at          TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    start_at   VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE time_slot
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    date             VARCHAR(255) NOT NULL,
    time_id          BIGINT,
    theme_id         BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255) NOT NULL,
    slot_id          BIGINT,
    is_deleted       BIGINT                DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES time_slot (id)
);

CREATE TABLE pending
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255) NOT NULL,
    slot_id          BIGINT,
    is_deleted       BIGINT                DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES time_slot (id)
);

CREATE UNIQUE INDEX unique_theme_name
    ON theme (name, deleted_at);

CREATE UNIQUE INDEX unique_time_start
    ON reservation_time (start_at, deleted_at);

CREATE UNIQUE INDEX unique_time_slot
    ON time_slot (date, time_id, theme_id);

CREATE UNIQUE INDEX unique_reservation
    ON reservation (slot_id, is_deleted);

CREATE UNIQUE INDEX unique_waiting
    ON pending (name, slot_id, is_deleted);
