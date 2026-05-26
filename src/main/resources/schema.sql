DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;

CREATE TABLE reservation_time (
  id BIGINT NOT NULL AUTO_INCREMENT,
  start_at VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE theme (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE reservation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  date date NOT NULL,
  time_id BIGINT NOT NULL,
  theme_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (time_id) REFERENCES reservation_time (id),
  FOREIGN KEY (theme_id) REFERENCES theme (id)
);
