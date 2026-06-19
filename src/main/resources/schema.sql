CREATE TABLE theme (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image VARCHAR(255),

    PRIMARY KEY (id),
    CONSTRAINT uk_theme_name UNIQUE (name)
);

CREATE TABLE reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_time_start_at UNIQUE (start_at)
);

CREATE TABLE reservation (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    theme_id BIGINT NOT NULL,
    date    DATE NOT NULL,
    time_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',

    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_theme_date_time UNIQUE (theme_id, date, time_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE CASCADE,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE CASCADE
);

CREATE TABLE reservation_waiting (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    theme_id    BIGINT       NOT NULL,
    date        DATE         NOT NULL,
    time_id     BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT uk_waiting_name_theme_date_time UNIQUE (name, theme_id, date, time_id),
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE CASCADE,
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE CASCADE
);

CREATE TABLE payment(
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    payment_key VARCHAR(255),
    order_id VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(300) NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE RESTRICT,
    UNIQUE (reservation_id),
    UNIQUE (idempotency_key),
    UNIQUE (order_id)
);
