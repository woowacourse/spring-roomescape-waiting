DROP TABLE IF EXISTS waiting;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme_slot;
DROP TABLE IF EXISTS time;
DROP TABLE IF EXISTS theme;

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme_slot
(
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    theme_id    BIGINT  NOT NULL,
    date        DATE    NOT NULL,
    time_id     BIGINT  NOT NULL,
    is_reserved BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (theme_id, date, time_id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    name                    VARCHAR(255) NOT NULL,
    status                  VARCHAR(255) NOT NULL,
    theme_slot_id           BIGINT       NOT NULL,
    confirmed_theme_slot_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN status = 'CONFIRMED' THEN theme_slot_id ELSE NULL END
    ),
    PRIMARY KEY (id),
    FOREIGN KEY (theme_slot_id) REFERENCES theme_slot (id),
    UNIQUE (confirmed_theme_slot_id)
);

CREATE TABLE IF NOT EXISTS waiting
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    member_name VARCHAR(255) NOT NULL,
    date        DATE         NOT NULL,
    time_id     BIGINT       NOT NULL,
    theme_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
