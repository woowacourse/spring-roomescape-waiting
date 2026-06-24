SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS time_slot CASCADE;
DROP TABLE IF EXISTS session CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS pending_payment CASCADE;
DROP TABLE IF EXISTS waiting CASCADE;

CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(250) NOT NULL,
    description   VARCHAR(250) NOT NULL,
    thumbnail_url VARCHAR(250) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE time_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_time_slot_start_at UNIQUE (start_at)
);

CREATE TABLE session
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES time_slot (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_session_date_time_theme UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    session_id  BIGINT       NOT NULL,
    amount      BIGINT       NOT NULL DEFAULT 0,
    payment_key VARCHAR(255) NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT uk_reservation_session UNIQUE (session_id)
);

CREATE TABLE pending_payment
(
    order_id   VARCHAR(64) NOT NULL,
    amount     BIGINT      NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id)
);

CREATE TABLE waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(250) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (session_id) REFERENCES session (id),
    CONSTRAINT uk_waiting_name_session UNIQUE (name, session_id)
);

DELETE FROM pending_payment;
TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE session RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE time_slot RESTART IDENTITY;

SET REFERENTIAL_INTEGRITY TRUE;
