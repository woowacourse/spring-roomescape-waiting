DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS waiting CASCADE;
DROP TABLE IF EXISTS time_slot CASCADE;

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

CREATE TABLE waiting
(
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name            VARCHAR(250) NOT NULL,
    date            DATE         NOT NULL,
    time_id         BIGINT       NOT NULL,
    theme_id        BIGINT       NOT NULL,
    PRIMARY KEY (name, date, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_date_time_theme UNIQUE (date, time_id, theme_id)
);
