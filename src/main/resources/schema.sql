CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(255) NOT NULL,
    description         VARCHAR(255) NOT NULL,
    is_active           TINYINT      NOT NULL DEFAULT 1,
    active_name         VARCHAR(255) AS (CASE WHEN is_active = 1 THEN name ELSE NULL END),
    PRIMARY KEY (id),
    CONSTRAINT uk_theme_active_name UNIQUE (active_name)
);

CREATE TABLE reservation_time
(
    id              BIGINT NOT NULL AUTO_INCREMENT,
    start_at        TIME   NOT NULL,
    is_active       TINYINT NOT NULL DEFAULT 1,
    active_start_at TIME AS (CASE WHEN is_active = 1 THEN start_at ELSE NULL END),
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_time_active_start_at UNIQUE (active_start_at)
);

CREATE TABLE reservation
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    date           DATE         NOT NULL,
    time_id        BIGINT,
    theme_id       BIGINT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP    NOT NULL,
    active_time_id BIGINT AS (CASE WHEN status = 'ACTIVE' THEN time_id ELSE NULL END),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_date_theme_active_time UNIQUE (date, theme_id, active_time_id)
);
