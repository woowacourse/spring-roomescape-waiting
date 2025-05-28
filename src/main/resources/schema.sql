CREATE TABLE IF NOT EXISTS reservation_time (
                                                id BIGINT NOT NULL AUTO_INCREMENT,
                                                start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS theme (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     role VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS reservation (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           date VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    reservation_time_id BIGINT,
    theme_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
    );
