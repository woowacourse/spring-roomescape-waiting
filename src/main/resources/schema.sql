CREATE TABLE IF NOT EXISTS reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME         NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    thumbnail   VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    status   VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',

    PRIMARY KEY (id),

    CONSTRAINT fk_reservation_time
        FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT fk_reservation_theme
        FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uq_reservation
        UNIQUE (date, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS reservation_waiting (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    date    DATE         NOT NULL,
    time_id BIGINT       NOT NULL,
    theme_id BIGINT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_reservation_waiting_time
        FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT fk_reservation_waiting_theme
        FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uq_reservation_waiting_name_slot
        UNIQUE (name, date, time_id, theme_id)
);
