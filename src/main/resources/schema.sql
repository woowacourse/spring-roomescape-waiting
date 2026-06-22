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
    slot_id    BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);

CREATE TABLE reservation
(
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50) NOT NULL,
    slot_id BIGINT      NOT NULL,
    status  VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    PRIMARY KEY (id),
    CONSTRAINT uq_reservation_slot UNIQUE (slot_id),
    FOREIGN KEY (slot_id) REFERENCES slot (id)
);

CREATE TABLE payment_order
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(64)  NOT NULL,
    amount         BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    reservation_id BIGINT       NOT NULL,
    payment_key    VARCHAR(200) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_payment_order_order_id       UNIQUE (order_id),
    CONSTRAINT uq_payment_order_reservation_id UNIQUE (reservation_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE INDEX idx_waiting_slot_created_id
    ON waiting (slot_id, created_at, id);
