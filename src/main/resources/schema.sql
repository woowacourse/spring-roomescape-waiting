CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE reservation_slot
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    date        DATE        NOT NULL,
    theme_id    BIGINT      NOT NULL,
    time_id     BIGINT      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES  theme (id),
    FOREIGN KEY (time_id) REFERENCES  reservation_time(id),
    UNIQUE (date, theme_id, time_id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    slot_id  BIGINT       NOT NULL,
    created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    UNIQUE (slot_id)
);

CREATE TABLE reservation_waiting
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    slot_id BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    requested_at   TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    UNIQUE (slot_id, name)
);


CREATE INDEX idx_reservation_name ON reservation (name);

CREATE INDEX idx_reservation_waiting_name ON reservation_waiting (name);

CREATE INDEX idx_reservation_waiting_sequence ON reservation_waiting (slot_id, requested_at, id);
