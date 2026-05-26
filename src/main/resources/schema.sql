CREATE TABLE reservation_time (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,
    deleted_at DATETIME DEFAULT NULL,
    active_start_at TIME GENERATED ALWAYS AS (
        CASE WHEN deleted_at IS NULL THEN start_at ELSE NULL END
    ),
    PRIMARY KEY (id)
);

CREATE TABLE theme (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_url VARCHAR(2000) NOT NULL,
    deleted_at DATETIME DEFAULT NULL,
    active_name VARCHAR(255) GENERATED ALWAYS AS (
        CASE WHEN deleted_at IS NULL THEN name ELSE NULL END
    ),
    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    time_id BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    canceled_at DATETIME DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    active_date DATE GENERATED ALWAYS AS (
        CASE WHEN deleted_at IS NULL AND canceled_at IS NULL THEN date ELSE NULL END
    ),
    active_time_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN deleted_at IS NULL AND canceled_at IS NULL THEN time_id ELSE NULL END
    ),
    active_theme_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN deleted_at IS NULL AND canceled_at IS NULL THEN theme_id ELSE NULL END
    ),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE UNIQUE INDEX uq_active_reservation
ON reservation (active_date, active_time_id, active_theme_id);

CREATE UNIQUE INDEX uq_active_reservation_time
ON reservation_time (active_start_at);

CREATE UNIQUE INDEX uq_active_theme
ON theme (active_name);
