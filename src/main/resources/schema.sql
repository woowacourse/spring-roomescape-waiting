-- 예약 시간
CREATE TABLE IF NOT EXISTS reservation_time (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

-- 테마
CREATE TABLE IF NOT EXISTS theme (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- 예약
CREATE TABLE IF NOT EXISTS reservation (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT,
    theme_id   BIGINT,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

-- 예약 대기
CREATE TABLE IF NOT EXISTS waiting (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT,
    theme_id   BIGINT,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);