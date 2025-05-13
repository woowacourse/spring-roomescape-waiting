CREATE TABLE IF NOT EXISTS reservations
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    user_id  BIGINT NOT NULL,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_times
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS themes
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT                   NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255)             NOT NULL,
    email    VARCHAR(255)             NOT NULL,
    password VARCHAR(255)             NOT NULL,
    role     ENUM ('NORMAL', 'ADMIN') NOT NULL,

    PRIMARY KEY (id)
);
