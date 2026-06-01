CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE reservation_time
(
    id           BIGINT NOT NULL AUTO_INCREMENT,
    start_at     TIME   NOT NULL,
    deleted_at   TIMESTAMP,
    delete_token BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (start_at, delete_token)
);

CREATE TABLE reservation
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    guest_name       VARCHAR(255) NOT NULL,
    date             DATE         NOT NULL,
    time_id          BIGINT       NOT NULL,
    theme_id         BIGINT       NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    last_modified_at TIMESTAMP    NOT NULL,
    confirm_token    varchar(36)  NOT NULL DEFAULT 0,
    waiting_token    varchar(36)  NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_confirmed_slot
        UNIQUE (date, time_id, theme_id, confirm_token),
    CONSTRAINT uk_reservation_waiting_guest_slot
        UNIQUE (guest_name, date, time_id, theme_id, waiting_token),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
