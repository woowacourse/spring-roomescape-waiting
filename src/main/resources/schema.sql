CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)   NOT NULL,
    description VARCHAR(1000) NOT NULL,
    thumbnail   VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    `date`   DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_slot_date_time_theme UNIQUE (`date`, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE waiting
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP   NOT NULL,
    slot_id    BIGINT,
    name       VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);

CREATE TABLE reservation
(
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50) NOT NULL,
    slot_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uq_reservation_slot UNIQUE (slot_id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);

-- 1순위 대기자 조회 + 순번 계산 서브쿼리
CREATE INDEX idx_waiting_slot_created_id
ON waiting (slot_id, created_at, id);
