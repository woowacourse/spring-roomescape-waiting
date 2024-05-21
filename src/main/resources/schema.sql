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
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at time   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    salt     VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    role     VARCHAR(255) check (role in ('USER','ADMIN')),
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    date      date   NOT NULL,
    time_id   BIGINT,
    theme_id  BIGINT,
    member_id BIGINT,
    status    VARCHAR(255) check (status in ('RESERVED')),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);
