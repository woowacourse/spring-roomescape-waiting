SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS waiting CASCADE;
DROP TABLE IF EXISTS time_slot CASCADE;

CREATE TABLE theme
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL
);

CREATE TABLE time_slot
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_at TIME NOT NULL
);

CREATE TABLE waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name       VARCHAR(250) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    PRIMARY KEY (name, date, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_date_time_theme UNIQUE (date, time_id, theme_id)
);

TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE time_slot RESTART IDENTITY;

INSERT INTO time_slot (start_at)
VALUES ('10:00:00'),
       ('12:00:00');
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('Theme1', 'Desc1', 'url1'),
       ('Theme2', 'Desc2', 'url2');

SET REFERENTIAL_INTEGRITY TRUE;
