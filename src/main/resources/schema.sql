drop table if exists payment_order;
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
                             status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                             enqueued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                             FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE payment_order (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             order_id VARCHAR(64) NOT NULL UNIQUE,
                             reserver_name VARCHAR(30) NOT NULL,
                             date DATE NOT NULL,
                             time_id BIGINT NOT NULL,
                             theme_id BIGINT NOT NULL,
                             amount BIGINT NOT NULL,
                             payment_key VARCHAR(200),
                             idempotency_key VARCHAR(300) NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                             reservation_id BIGINT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                             FOREIGN KEY (theme_id) REFERENCES theme (id),
                             FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);
