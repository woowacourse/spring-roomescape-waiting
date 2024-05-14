DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS member;

CREATE TABLE member
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
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

CREATE TABLE reservation
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    member_id   BIGINT       NOT NULL,
    date        DATE         NOT NULL,
    time_id     BIGINT       NOT NULL,
    theme_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id)   REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id)  REFERENCES theme (id)
);

INSERT INTO member (name, email, password, role) VALUES ('냥인', 'nyangin@email.com', '1234', 'ADMIN');
INSERT INTO member (name, email, password, role) VALUES ('미아', 'mia@email.com', '1234', 'MEMBER');
