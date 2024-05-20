
CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
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
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS reservation
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    reservation_time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,                             -- 컬럼 추가
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id) -- 외래키 추가
    );

CREATE TABLE IF NOT EXISTS member_reservation
(
    id             BIGINT NOT NULL AUTO_INCREMENT,
    member_id      BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    created_time DATETIME NOT NULL,
    status VARCHAR(255),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    PRIMARY KEY (id)
    );
