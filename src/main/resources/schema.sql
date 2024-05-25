CREATE TABLE IF NOT EXISTS member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(50) NOT NULL,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(64) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member_role (
    member_id BIGINT NOT NULL,
    role VARCHAR(10) NOT NULL,
    PRIMARY KEY (member_id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE IF NOT EXISTS theme (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(200) NOT NULL,
    thumbnail_url VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_time (
     id BIGINT NOT NULL AUTO_INCREMENT,
     start_at TIME NOT NULL,
     PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    date DATE NOT NULL,
    time_id BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

