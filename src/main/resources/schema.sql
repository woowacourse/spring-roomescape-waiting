CREATE TABLE IF NOT EXISTS reservation_time
(
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    start_at    TIME      NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255)    NOT NULL UNIQUE,
    description      VARCHAR(255)    NOT NULL,
    thumbnail_url    TEXT            NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS schedule
(
    id           BIGINT    NOT NULL AUTO_INCREMENT,
    date         DATE      NOT NULL,
    time_id      BIGINT    NOT NULL,
    theme_id     BIGINT    NOT NULL,
    PRIMARY KEY  (id),

    CONSTRAINT unique_schedule UNIQUE(date, time_id, theme_id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255)    NOT NULL,
    schedule_id    BIGINT          NOT NULL,
    status         VARCHAR(20)     NOT NULL DEFAULT 'RESERVED',
    updated_at     DATETIME        NOT NULL,
    PRIMARY KEY    (id),

    CONSTRAINT check_reservation_status CHECK (status IN ('RESERVED', 'WAITING', 'CANCELED')),
    FOREIGN KEY (schedule_id) REFERENCES schedule (id)
);

CREATE INDEX idx_reservation_schedule_id
    ON reservation(schedule_id);
