DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_slot;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;

CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url TEXT NOT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS reservation_slot
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    date     DATE NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT unique_reservation
    UNIQUE(date, time_id, theme_id),

    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    reservation_slot_id  BIGINT       NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT check_reservation_status CHECK (status IN ('RESERVED','WAITING','CANCELED')),

    FOREIGN KEY (reservation_slot_id) REFERENCES reservation_slot (id)
);