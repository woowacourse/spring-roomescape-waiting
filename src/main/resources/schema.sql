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
    img_url     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
        CHECK (role IN ('USER', 'MANAGER')),
    store_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uq_member_email UNIQUE (email),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE reservation
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    date      VARCHAR(255) NOT NULL,
    time_id   BIGINT       NOT NULL,
    theme_id  BIGINT       NOT NULL,
    store_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uq_reservation_store_date_time_theme UNIQUE (store_id, date, time_id, theme_id)
);

CREATE TABLE reservation_wait
(
    id             BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    member_id      BIGINT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT uq_reservation_wait_reservation_member UNIQUE (reservation_id, member_id)
);

CREATE INDEX idx_wait_reservation_created ON reservation_wait (reservation_id, created_at);
CREATE INDEX idx_wait_member ON reservation_wait (member_id);
