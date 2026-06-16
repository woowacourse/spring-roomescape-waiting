CREATE TABLE reservation_time (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,
    status ENUM('ACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    active_start_at TIME GENERATED ALWAYS AS (
        CASE WHEN status = 'ACTIVE' THEN start_at END
    ),
    PRIMARY KEY (id)
);

CREATE TABLE theme (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_url VARCHAR(2000) NOT NULL,
    status ENUM('ACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    active_name VARCHAR(255) GENERATED ALWAYS AS (
        CASE WHEN status = 'ACTIVE' THEN name END
    ),
    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    time_id BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'CANCELED', 'DELETED', 'WAITING') DEFAULT 'ACTIVE',
    order_status ENUM('PENDING', 'CONFIRMED') NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    active_flag BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status = 'ACTIVE' THEN true END
    ),
    waiting_flag BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status = 'WAITING' THEN true END
    ),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE UNIQUE INDEX uq_waiting_reservation
ON reservation (waiting_flag, name, date, time_id, theme_id);

CREATE UNIQUE INDEX uq_active_reservation
ON reservation (active_flag, date, time_id, theme_id);

CREATE UNIQUE INDEX uq_active_reservation_time
ON reservation_time (active_start_at);

CREATE UNIQUE INDEX uq_active_theme
ON theme (active_name);

CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
