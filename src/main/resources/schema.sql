CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL UNIQUE CHECK (email like '%@%'),
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    member_id           BIGINT       NOT NULL,
    theme_id            BIGINT       NOT NULL,
    date                VARCHAR(255) NOT NULL,
    reservation_time_id BIGINT       NOT NULL,
    status  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (reservation_time_id) REFERENCES reservation_time (id)
);
