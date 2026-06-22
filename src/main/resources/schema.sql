CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255) NOT NULL,
    thumbnail_img_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    date        DATE         NOT NULL,
    theme_id    BIGINT       NOT NULL,
    time_id     BIGINT       NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    order_id    VARCHAR(64),
    amount      BIGINT,
    payment_key VARCHAR(200),
    idempotency_key VARCHAR(300),
    PRIMARY KEY (id),
    CONSTRAINT uq_reservation_order_id UNIQUE (order_id),
    CONSTRAINT uq_reservation_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uq_reservation_date_theme_time UNIQUE (date, theme_id, time_id)
);

CREATE TABLE waiting
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_waiting_date_theme_time UNIQUE (name, date, theme_id, time_id)
);
