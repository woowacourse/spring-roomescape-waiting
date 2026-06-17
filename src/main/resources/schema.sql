CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    end_at   TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    price         BIGINT       NOT NULL DEFAULT 30000,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    date           DATE         NOT NULL,
    time_id        BIGINT       NOT NULL,
    theme_id       BIGINT       NOT NULL,
    payment_status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    -- 추후 일정 시간이 지나도 결제 완료 처리가 되지 않을 시 삭제 처리 할 수 있도록 created_at 추가 요망
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (date, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS payment
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(255) NOT NULL,
    reservation_id BIGINT       NOT NULL,
    amount         BIGINT       NOT NULL,
    paymentkey     VARCHAR(255) NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE TABLE IF NOT EXISTS waiting_list
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (name, date, time_id, theme_id)
);
