CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(20)  NOT NULL,
    description         VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(500) NOT NULL,
    price               INT          NOT NULL DEFAULT 0,
    is_active           TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_theme_name UNIQUE (name)
);

CREATE TABLE reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME         NOT NULL,
    status   VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_time_start_at UNIQUE (start_at)
);

CREATE TABLE reservation_slot (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    date    DATE         NOT NULL,
    theme_id BIGINT,
    time_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    CONSTRAINT uk_reservation_date_theme_time UNIQUE (date, theme_id, time_id)
);

CREATE TABLE reservation (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(20)  NOT NULL,
    slot_id BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    active_status  VARCHAR(20)  NOT NULL,

    -- 결제 완료로 ACTIVE 된 같은 슬롯/이름 중복을 DB 레벨에서 방지하기 위한 파생 컬럼입니다.
    -- PENDING/CANCELED 이력은 active_name이 NULL이 되어 같은 이름으로 여러 번 남길 수 있습니다.
    active_name    VARCHAR(20) GENERATED ALWAYS AS (
        CASE WHEN active_status = 'ACTIVE' THEN name ELSE NULL END
    ),
    created_at     TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id),
    CONSTRAINT uk_reservation_slot_active_name UNIQUE (slot_id, active_name)
);

CREATE TABLE orders (
    order_id       VARCHAR(64)  NOT NULL,
    target_id      BIGINT       NOT NULL,
    order_type     VARCHAR(20)  NOT NULL,
    order_name     VARCHAR(100) NOT NULL,
    amount         BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    PRIMARY KEY (order_id),
    FOREIGN KEY (target_id) REFERENCES reservation (id)
);

CREATE TABLE payment_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    order_id       VARCHAR(64)  NOT NULL,
    payment_key    VARCHAR(200),
    amount         BIGINT       NOT NULL,
    status         VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_history_order_id UNIQUE (order_id),
    CONSTRAINT uk_payment_history_payment_key UNIQUE (payment_key),
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
);
