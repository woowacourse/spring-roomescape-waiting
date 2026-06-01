CREATE TABLE reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    url         VARCHAR(512) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (date, theme_id, time_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE RESTRICT,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE RESTRICT
);

CREATE TABLE reservation
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    slot_id    BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (slot_id),
    FOREIGN KEY (slot_id) REFERENCES slot (id) ON DELETE RESTRICT
);

CREATE TABLE waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    slot_id    BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (slot_id, name),
    FOREIGN KEY (slot_id) REFERENCES slot (id) ON DELETE RESTRICT
);
