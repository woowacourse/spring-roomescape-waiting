CREATE TABLE IF NOT EXISTS members
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL CHECK (role IN ('어드민', '회원')),
    UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS reservation_times
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE IF NOT EXISTS themes
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS reservations
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    date      VARCHAR(255) NOT NULL,
    time_id   BIGINT       NOT NULL,
    theme_id  BIGINT       NOT NULL,
    member_id BIGINT       NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_times (id),
    FOREIGN KEY (theme_id) REFERENCES themes (id),
    FOREIGN KEY (member_id) REFERENCES members (id)
);
