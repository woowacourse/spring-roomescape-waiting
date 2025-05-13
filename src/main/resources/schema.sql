-- 테이블이 이미 존재할 경우를 대비해 삭제
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS users;

-- 유저 테이블
CREATE TABLE users
(
    id       VARCHAR(255) NOT NULL,
    role     VARCHAR(30)  NOT NULL,
    email    VARCHAR(30)  NOT NULL,
    name     VARCHAR(30)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- 테마 테이블
CREATE TABLE theme
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(30)  NOT NULL,
    description VARCHAR(30)  NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- 예약 시간 테이블
CREATE TABLE reservation_time
(
    id       VARCHAR(255) NOT NULL,
    start_at VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id)
);

-- 예약 테이블
CREATE TABLE reservation
(
    id       VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  VARCHAR(255) NOT NULL,
    theme_id VARCHAR(255) NOT NULL,
    user_id  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
