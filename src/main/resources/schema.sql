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

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    UNIQUE (date, theme_id, time_id)
);

CREATE TABLE reservation_waiting
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    name     VARCHAR(255) NOT NULL,
    waiting_time TIME NOT NULL
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    UNIQUE (waiting_time)
)