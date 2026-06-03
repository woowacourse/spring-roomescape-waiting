CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(20)  NOT NULL,
    description         VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(500) NOT NULL,
    is_active           TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_theme_name UNIQUE (name)
);

CREATE TABLE reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME         NOT NULL,
    status   VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_time_start_at UNIQUE (start_at)
);

CREATE TABLE reservation_slot (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    date    DATE         NOT NULL,
    theme_id BIGINT,
    time_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT uk_reservation_date_theme_time UNIQUE (date, theme_id, time_id)
);

CREATE TABLE reservation (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(20)  NOT NULL,
    slot_id BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    active_status  VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT uk_reservation_slot_name_active_status UNIQUE (slot_id, name, active_status)
);
