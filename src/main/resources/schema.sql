CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT       NOT NULL,
--     TODO: UNIQUE 속성 넣을지 고민하기
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id          BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member
(
    id       BIGINT       NOT NULL,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id        BIGINT       NOT NULL,
    date      VARCHAR(255) NOT NULL,
    time_id   BIGINT,
    theme_id  BIGINT,
    member_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

