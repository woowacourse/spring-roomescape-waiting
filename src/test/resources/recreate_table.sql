DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS member;

CREATE TABLE reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    date      VARCHAR(255) NOT NULL,
    time_id   BIGINT,
    theme_id  BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

INSERT INTO member(name, email, password) VALUES ( '클로버', 'test@gmail.com', 'password' );
INSERT INTO member(name, email, password) VALUES ( '페드로', 'test2@gmail.com', 'password' );
INSERT INTO member(name, email, password) VALUES ( '클로버2', 'test3@gmail.com', 'password' );
INSERT INTO member(name, email, password) VALUES ( '클로버3', 'test4@gmail.com', 'password' );
INSERT INTO member(name, email, password) VALUES ( '클로버4', 'test5@gmail.com', 'password' );
INSERT INTO member(name, email, password) VALUES ( '클로버5', 'test6@gmail.com', 'password' );
INSERT INTO member(name, email, password, role) VALUES ( '관리자', 'admin@gmail.com', 'password', 'ADMIN' );

INSERT INTO reservation_time (start_at) VALUES ('10:00:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00:00');
INSERT INTO theme (name, description, thumbnail) VALUES ( '공포', '완전 무서운 테마', 'https://example.org' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링', '완전 힐링되는 테마', 'https://example.org' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링2', '완전 힐링되는 테마2', 'https://example.org' );
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 2, '2099-12-31', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, '2099-12-31', 1, 2);

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, '2024-12-01', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 3, '2024-12-02', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 4, '2024-12-02', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 5, '2024-12-03', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 6, '2024-12-04', 1, 2);

// 인기 테마 검증용
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 4, FORMATDATETIME(DATEADD('DAY', -3, NOW()), 'yyyy-MM-dd'), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 5, FORMATDATETIME(DATEADD('DAY', -4, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 6, FORMATDATETIME(DATEADD('DAY', -5, NOW()), 'yyyy-MM-dd'), 1, 1);
