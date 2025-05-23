DROP TABLE IF EXISTS reservation_schedule;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_wait;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS theme;

CREATE TABLE member
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_schedule
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    time_id BIGINT,
    theme_id BIGINT,
    date VARCHAR(255) NOT NULL,
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
)

CREATE TABLE reservation
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT,
    member_id BIGINT,
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (schedule_id) REFERENCES reservation_schedule (id),
    UNIQUE (schedule_id)
);

CREATE TABLE reservation_wait
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT,
    member_id BIGINT,
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (schedule_id) REFERENCES reservation_schedule (id),
    UNIQUE (schedule_id, member_id)
);
