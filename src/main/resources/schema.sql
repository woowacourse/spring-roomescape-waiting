-- 여러 테스트 컨텍스트가 같은 in-memory DB 를 공유할 때 재실행돼도 안전하도록 FK 역순으로 먼저 비운다.
DROP TABLE IF EXISTS reservation_waiting;
DROP TABLE IF EXISTS payment_order;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

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
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_theme_date_time UNIQUE (theme_id, date, time_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE CASCADE,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE CASCADE
);

CREATE TABLE payment_order (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    order_id        VARCHAR(255) NOT NULL,
    amount          BIGINT       NOT NULL,
    reservation_id  BIGINT       NOT NULL,
    idempotency_key VARCHAR(300) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    payment_key     VARCHAR(255),
    approved_amount BIGINT,

    PRIMARY KEY (id),
    CONSTRAINT uk_payment_order_order_id UNIQUE (order_id),
    CONSTRAINT uk_payment_order_idempotency_key UNIQUE (idempotency_key),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE CASCADE
);

CREATE TABLE reservation_waiting (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    theme_id BIGINT NOT NULL,
    date    DATE NOT NULL,
    time_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_name_reservation_theme_date_time UNIQUE (name, theme_id, date, time_id),
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE CASCADE,
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE CASCADE
);
