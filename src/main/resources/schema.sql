drop table if exists payment;
drop table if exists waiting;
drop table if exists reservation;
drop table if exists reservation_time;
drop table if exists theme;

CREATE TABLE reservation_time (
                                  id       BIGINT       NOT NULL AUTO_INCREMENT,
                                  start_at TIME NOT NULL,
                                  PRIMARY KEY (id),
                                  UNIQUE (start_at)
);

CREATE TABLE theme (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(30) NOT NULL ,
                       description   VARCHAR(255) NOT NULL ,
                       thumbnail_url  VARCHAR(255) NOT NULL,
                       PRIMARY KEY (id)
);

CREATE TABLE reservation (
                             id      BIGINT       NOT NULL AUTO_INCREMENT,
                             name    VARCHAR(30) NOT NULL,
                             date    DATE NOT NULL,
                             time_id BIGINT NOT NULL,
                             theme_id BIGINT NOT NULL,
                             status  VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                             PRIMARY KEY (id),
                             FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                             FOREIGN KEY (theme_id) REFERENCES theme (id),
                             UNIQUE (date, time_id, theme_id)
);

CREATE TABLE waiting (
                         id         BIGINT       NOT NULL AUTO_INCREMENT,
                         name       VARCHAR(30) NOT NULL,
                         date       DATE NOT NULL,
                         time_id    BIGINT       NOT NULL,
                         theme_id   BIGINT       NOT NULL,
                         order_index INT          NOT NULL,
                         PRIMARY KEY (id),
                         FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                         FOREIGN KEY (theme_id) REFERENCES theme (id),
                         UNIQUE (date, time_id, theme_id, order_index),
                         UNIQUE (date, time_id, theme_id, name)
);

CREATE TABLE payment (
                         id             BIGINT       NOT NULL AUTO_INCREMENT,
                         reservation_id BIGINT       NOT NULL,
                         order_id       VARCHAR(64)  NOT NULL,
                         amount         BIGINT       NOT NULL,
                         payment_key    VARCHAR(255),
                         status         VARCHAR(20)  NOT NULL,
                         PRIMARY KEY (id),
                         FOREIGN KEY (reservation_id) REFERENCES reservation (id),
                         UNIQUE (order_id),
                         UNIQUE (reservation_id)
);
