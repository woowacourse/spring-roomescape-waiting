CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)  NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE schedule
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE RESTRICT ,
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE RESTRICT
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE waiting
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    member_id   BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    member_id   BIGINT NOT NULL,
    schedule_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE RESTRICT,
    FOREIGN KEY (schedule_id) REFERENCES schedule (id) ON DELETE RESTRICT
);
