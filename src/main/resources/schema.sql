CREATE TABLE member
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);


CREATE TABLE theme
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail VARCHAR(255) NOT NULL
);

CREATE TABLE reservation_time
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_at VARCHAR(255) NOT NULL
);

CREATE TABLE reservation
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    member_id BIGINT,
    FOREIGN KEY (time_id) REFERENCES reservation_time(id),
    FOREIGN KEY (theme_id) REFERENCES theme(id),
    FOREIGN KEY (member_id) REFERENCES member(id)
);
