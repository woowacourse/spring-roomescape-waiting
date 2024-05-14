CREATE TABLE member
(
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    name  VARCHAR(10)  NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    role  VARCHAR(5)   DEFAULT 'USER' NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME         NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(500) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    date      DATE         NOT NULL,
    member_id BIGINT       NOT NULL,
    time_id   BIGINT       NOT NULL,
    theme_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (date, time_id, theme_id)
);
