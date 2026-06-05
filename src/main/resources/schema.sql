DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS slot;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE theme (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(20)  NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE slot (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uq_slot UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    slot_id BIGINT      NOT NULL,
    name    VARCHAR(20) NOT NULL,
    status  VARCHAR(10) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id),
    CONSTRAINT uq_reservation UNIQUE (slot_id, name)
);
