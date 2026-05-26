CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_date
(
    id   BIGINT NOT NULL AUTO_INCREMENT,
    date DATE   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,
    url     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date_id  BIGINT,
    time_id  BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
