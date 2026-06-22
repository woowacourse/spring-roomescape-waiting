CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE theme
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(20)  NOT NULL,
    description  VARCHAR(255) NOT NULL,
    image_url    VARCHAR(255) NOT NULL,
    running_time BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_theme_name UNIQUE (name)
);


CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    slot_id          BIGINT      NOT NULL,
    name             VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    created_at       DATETIME    NOT NULL,
    occupied_slot_id BIGINT GENERATED ALWAYS AS (CASE WHEN status IN ('CONFIRMED', 'PENDING') THEN slot_id END),
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id),
    CONSTRAINT uq_applicant UNIQUE (slot_id, name),
    CONSTRAINT uq_occupied UNIQUE (occupied_slot_id)
);

CREATE TABLE orders
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    order_id       VARCHAR(64)  NOT NULL,
    amount         BIGINT       NOT NULL,
    payment_key    VARCHAR(210),
    status         VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE CASCADE,
    CONSTRAINT uq_order_id UNIQUE (order_id)
);
