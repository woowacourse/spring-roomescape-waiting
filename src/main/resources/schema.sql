CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_date
(
    id   BIGINT NOT NULL AUTO_INCREMENT,
    date DATE   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,
    url     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE users
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date_id  BIGINT,
    time_id  BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE (date_id, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE user_reservation
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    reservation_id BIGINT       NOT NULL,
    waiting_number BIGINT,
    status         VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE CASCADE
);
