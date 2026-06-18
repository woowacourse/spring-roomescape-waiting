CREATE TABLE IF NOT EXISTS reservation_time
(
    id        BIGINT  NOT NULL AUTO_INCREMENT,
    start_at  TIME    NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_date
(
    id        BIGINT  NOT NULL AUTO_INCREMENT,
    date      DATE    NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member
(
    id       BIGINT                     NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255)               NOT NULL,
    password VARCHAR(255)               NOT NULL,
    role     ENUM ('MEMBER', 'MANAGER') NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date_id  BIGINT NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (date_id, time_id, theme_id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id            BIGINT                                   NOT NULL AUTO_INCREMENT,
    member_id     BIGINT                                   NOT NULL,
    date_id       BIGINT                                   NOT NULL,
    time_id       BIGINT                                   NOT NULL,
    theme_id      BIGINT                                   NOT NULL,
    waiting_order BIGINT                                   NOT NULL,
    status        ENUM ('RESERVED', 'WAITING', 'CANCELED') NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE INDEX IF NOT EXISTS idx_theme_is_active ON theme (is_active);
CREATE INDEX IF NOT EXISTS idx_reservation_time_is_active ON reservation_date (is_active);
CREATE INDEX IF NOT EXISTS idx_reservation_date_is_active ON reservation_time (is_active);
CREATE INDEX IF NOT EXISTS idx_reservation_date_time_theme_status ON reservation (date_id, time_id, theme_id, status);
CREATE INDEX IF NOT EXISTS idx_reservation_member_id ON reservation (member_id);
