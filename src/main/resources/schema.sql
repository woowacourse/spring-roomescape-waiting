CREATE TABLE theme
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(255)  NOT NULL,
    thumbnail   VARCHAR(2048) NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE reservation_time
(
    id        BIGINT  NOT NULL AUTO_INCREMENT,
    start_at  TIME    NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE SEQUENCE reservation_request_order_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE reservation
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    date          DATE         NOT NULL,
    time_id       BIGINT       NOT NULL,
    theme_id      BIGINT       NOT NULL,
    request_order BIGINT       NOT NULL,
    created_at    TIMESTAMP    NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (theme_id, date, time_id, name),
--  데이터베이스 정합성을 위해서 필요하나, insert시 성능 문제가 있어서 제외한다.
--  UNIQUE (request_order),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation_history
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    date           DATE         NOT NULL,
    time_id        BIGINT       NOT NULL,
    theme_id       BIGINT       NOT NULL,
    request_order  BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    canceled_at    TIMESTAMP    NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE payment_order
(
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    order_id        VARCHAR(64)   NOT NULL,
    idempotency_key VARCHAR(300)  NOT NULL,
    amount          BIGINT        NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    name            VARCHAR(255)  NOT NULL,
    date            DATE          NOT NULL,
    time_id         BIGINT        NOT NULL,
    theme_id        BIGINT        NOT NULL,
    payment_key     VARCHAR(2048),
    reservation_id  BIGINT,
    failure_code    VARCHAR(255),
    failure_message VARCHAR(1000),
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    confirmed_at    TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE (order_id),
    UNIQUE (idempotency_key),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE INDEX idx_reservation_slot_request_order
    ON reservation (theme_id, date, time_id, request_order);

CREATE INDEX idx_reservation_name
    ON reservation (name);

CREATE INDEX idx_reservation_history_name
    ON reservation_history (name);

CREATE INDEX idx_payment_order_slot_name_status
    ON payment_order (theme_id, date, time_id, name, status);
