CREATE TABLE IF NOT EXISTS reservation_time (
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    start_at    TIME      NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    name             VARCHAR(255)    NOT NULL UNIQUE,
    description      VARCHAR(255)    NOT NULL,
    thumbnail_url    TEXT            NOT NULL,
    price            INT             NOT NULL,
    PRIMARY KEY      (id)
);

CREATE TABLE IF NOT EXISTS schedule (
    id           BIGINT    NOT NULL AUTO_INCREMENT,
    date         DATE      NOT NULL,
    time_id      BIGINT    NOT NULL,
    theme_id     BIGINT    NOT NULL,
    PRIMARY KEY  (id),

    CONSTRAINT unique_schedule UNIQUE(date, time_id, theme_id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT           NOT NULL AUTO_INCREMENT,
    login_id    VARCHAR(255)     NOT NULL UNIQUE,
    name        VARCHAR(10)      NOT NULL,
    password    VARCHAR(255)     NOT NULL,
    role        VARCHAR(20)      NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT check_member_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS reservation (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    user_id        BIGINT          NOT NULL,
    schedule_id    BIGINT          NOT NULL,
    status         VARCHAR(20)     NOT NULL DEFAULT 'RESERVED',
    updated_at     DATETIME        NOT NULL,
    PRIMARY KEY    (id),

    CONSTRAINT check_reservation_status CHECK (status IN ('RESERVED', 'WAITING', 'CANCELED')),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (schedule_id) REFERENCES schedule (id)
);

CREATE INDEX idx_reservation_schedule_id
    ON reservation(schedule_id);

CREATE INDEX idx_reservation_user_schedule_status
    ON reservation(user_id, schedule_id, status);

CREATE TABLE IF NOT EXISTS payment_order (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    order_id         VARCHAR(64)     NOT NULL UNIQUE,
    user_id          BIGINT          NOT NULL,
    schedule_id      BIGINT          NOT NULL,
    amount           INT             NOT NULL,
    status           VARCHAR(20)     NOT NULL,
    payment_key      VARCHAR(200),
    reservation_id   BIGINT,
    failure_code     VARCHAR(100),
    failure_message  VARCHAR(510),
    created_at       DATETIME        NOT NULL,
    updated_at       DATETIME        NOT NULL,
    PRIMARY KEY (id),

    CONSTRAINT check_payment_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED')),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE INDEX idx_payment_order_schedule_status
    ON payment_order(schedule_id, status);
