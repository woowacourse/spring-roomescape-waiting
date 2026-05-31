CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(30)  NOT NULL,
    description VARCHAR(100) NOT NULL,
    thumbnail   VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    `date`   DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE waiting
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP   NOT NULL,
    slot_id    BIGINT,
    name       VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);

CREATE TABLE reservation
(
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    name    VARCHAR(10) NOT NULL,
    slot_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);
