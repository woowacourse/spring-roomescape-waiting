CREATE TABLE reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME         NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255) NOT NULL,
    thumbnail_img_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    status   VARCHAR(32)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PAYMENT_PENDING', 'CONFIRMED', 'CANCELED')),
    CONSTRAINT uk_reservation_date_theme_time UNIQUE (date, theme_id, time_id)
);

CREATE TABLE payment_order (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT      NOT NULL,
    order_id       VARCHAR(64) NOT NULL,
    amount         BIGINT      NOT NULL,
    payment_key    VARCHAR(255),
    status         VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT uk_payment_order_order_id UNIQUE (order_id),
    CONSTRAINT uk_payment_order_payment_key UNIQUE (payment_key),
    CONSTRAINT chk_payment_order_id_length CHECK (CHAR_LENGTH(order_id) BETWEEN 6 AND 64),
    CONSTRAINT chk_payment_order_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED', 'CANCELED'))
);

CREATE TABLE waiting (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    rank INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT uk_waiting UNIQUE (name, date, theme_id, time_id)
);
