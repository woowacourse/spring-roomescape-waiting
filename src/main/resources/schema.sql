drop table if exists member CASCADE;
drop table if exists theme CASCADE;
drop table if exists reservation CASCADE;
drop table if exists reservation_time CASCADE;

CREATE TABLE member
(
    id       BIGINT        NOT NULL AUTO_INCREMENT,
    name     VARCHAR(20)   NOT NULL,
    email    VARCHAR(30)   NOT NULL UNIQUE,
    password VARCHAR(1000) NOT NULL,
    role     VARCHAR(20)   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description VARCHAR(500)  NOT NULL,
    thumbnail   VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT     NOT NULL AUTO_INCREMENT,
    date     DATE       NOT NULL,
    time_id  BIGINT,
    theme_id BIGINT,
    member_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);
