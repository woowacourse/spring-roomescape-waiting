DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS holiday;

CREATE TABLE reservation_time
(
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    start_time DATETIME NOT NULL,
    end_time   DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    time_id     BIGINT       NOT NULL,
    theme_id    BIGINT       NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    order_id    VARCHAR(64)  NULL,
    amount      BIGINT       NULL,
    payment_key VARCHAR(200) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uq_reservation_name_theme_time UNIQUE (name, theme_id, time_id)
);

CREATE TABLE holiday
(
    id   BIGINT NOT NULL AUTO_INCREMENT,
    date DATE   NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_reservation_theme ON reservation (theme_id);
CREATE INDEX idx_holiday_date ON holiday (date);
