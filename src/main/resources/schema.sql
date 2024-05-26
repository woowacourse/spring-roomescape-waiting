CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE IF NOT EXISTS theme (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theme_name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservation_time (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_at TIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    member_id BIGINT,
    status VARCHAR(255) NOT NULL CHECK (status IN ('RESERVED', 'WAITING')),
    FOREIGN KEY (time_id) REFERENCES reservation_time(id),
    FOREIGN KEY (theme_id) REFERENCES theme(id),
    FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE  IF NOT EXISTS waiting (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT,
  waiting_order INTEGER,
FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);
