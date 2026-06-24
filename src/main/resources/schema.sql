CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL UNIQUE,

    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(255) NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT unique_reservation_date_time_theme
        UNIQUE (date, theme_id, time_id)
);

CREATE TABLE reservation_order
(
    order_id         VARCHAR(64)  NOT NULL,
    order_name       VARCHAR(255) NOT NULL,
    amount           BIGINT       NOT NULL,
    payment_key      VARCHAR(255),
    status           VARCHAR(30)  NOT NULL,
    reserver_name    VARCHAR(255) NOT NULL,
    reservation_date DATE         NOT NULL,
    time_id          BIGINT       NOT NULL,
    theme_id         BIGINT       NOT NULL,

    PRIMARY KEY (order_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE reservation_waiting
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    created_at TIMESTAMP    NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT unique_reservation_waiting_date_time_theme_name
        UNIQUE (date, theme_id, time_id, name)
);
