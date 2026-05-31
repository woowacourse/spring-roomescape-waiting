drop table if exists reservation;
drop table if exists reservation_time;
drop table if exists theme;

CREATE TABLE reservation_time (
                                  id       BIGINT       NOT NULL AUTO_INCREMENT,
                                  start_at TIME NOT NULL,
                                  PRIMARY KEY (id)
);

CREATE TABLE theme (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL UNIQUE,
  description   VARCHAR(255) NOT NULL ,
  thumbnail_url  VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE reservation (
                             id      BIGINT       NOT NULL AUTO_INCREMENT,
                             reserver_name VARCHAR(30) NOT NULL,
                             date    DATE NOT NULL,
                             time_id BIGINT NOT NULL,
                             theme_id BIGINT NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                             FOREIGN KEY (theme_id) REFERENCES theme (id),
                             UNIQUE (reserver_name, date, time_id, theme_id)
);
