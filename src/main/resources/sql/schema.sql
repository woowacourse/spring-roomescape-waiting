CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE IF NOT EXISTS reservation_time
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

CREATE TABLE IF NOT EXISTS
    reservation_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    theme_id BIGINT NOT NULL,
    time_id  BIGINT NOT NULL,
    date     DATE   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS waiting
(
    id                  BIGINT    NOT NULL AUTO_INCREMENT,
    member_id           BIGINT    NOT NULL,
    reservation_slot_id BIGINT    NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (reservation_slot_id) REFERENCES reservation_slot (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    member_id           BIGINT NOT NULL,
    reservation_slot_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (reservation_slot_id) REFERENCES reservation_slot (id)
);
