CREATE TABLE IF NOT EXISTS theme (
   id BIGINT NOT NULL AUTO_INCREMENT,
   name VARCHAR(255) NOT NULL,
   description TEXT NOT NULL,
   thumbnail TEXT NOT NULL,
   PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_time (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date VARCHAR(255) NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PAYMENT_PENDING',
    order_id VARCHAR(64),
    amount BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    payment_key VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE TABLE IF NOT EXISTS waiting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date VARCHAR(255) NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
