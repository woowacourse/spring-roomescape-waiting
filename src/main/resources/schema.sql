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

CREATE TABLE reservation_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    `date`   DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_slot UNIQUE (`date`, time_id, theme_id)
);

CREATE TABLE reservation
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(10) NOT NULL,
    slot_id     BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,
    payment_key VARCHAR(200),
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT uk_reservation_slot_id UNIQUE (slot_id)
);

CREATE TABLE reservation_waiting
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(10) NOT NULL,
    created_at DATETIME    NOT NULL,
    slot_id    BIGINT      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT uk_reservation_waiting_slot_id_name UNIQUE (name, slot_id)
);

CREATE TABLE payment_order
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(64) NOT NULL,
    reservation_id BIGINT      NOT NULL,
    amount         BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL,
    created_at     DATETIME    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT uk_payment_order_order_id UNIQUE (order_id)
);
