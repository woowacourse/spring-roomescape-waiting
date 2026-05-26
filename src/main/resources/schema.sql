CREATE TABLE theme (
   id BIGINT NOT NULL AUTO_INCREMENT,
   name VARCHAR(255) NOT NULL,
   description TEXT NOT NULL,
   thumbnail TEXT NOT NULL,
   PRIMARY KEY (id)
);

CREATE TABLE reservation_time (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date VARCHAR(255) NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE waiting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date VARCHAR(255) NOT NULL,
    time_id BIGINT,
    theme_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
