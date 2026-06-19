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
    date DATE NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_reservation_schedule UNIQUE (date, time_id, theme_id)
);

CREATE INDEX IF NOT EXISTS idx_reservation_name ON reservation (name);

CREATE TABLE IF NOT EXISTS waiting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    CONSTRAINT uk_waiting_schedule_name UNIQUE (date, time_id, theme_id, name)
);

CREATE INDEX IF NOT EXISTS idx_waiting_schedule ON waiting (theme_id, date, time_id, id);
CREATE INDEX IF NOT EXISTS idx_waiting_name ON waiting (name);

CREATE TABLE IF NOT EXISTS payment_order (
    order_id VARCHAR(64) NOT NULL,
    reservation_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    payment_key VARCHAR(255),
    PRIMARY KEY (order_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id) ON DELETE CASCADE
);
