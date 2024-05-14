DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS member;

CREATE TABLE IF NOT EXISTS reservation_time
(
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  start_at VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  thumbnail VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS member
(
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS reservation
(
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  date VARCHAR(255) NOT NULL,
  time_id BIGINT    NOT NULL,
  theme_id BIGINT   NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (member_id) REFERENCES member (id),
  FOREIGN KEY (time_id) REFERENCES reservation_time (id),
  FOREIGN KEY (theme_id) REFERENCES theme (id)
);

INSERT INTO member(name, email, password,role) VALUES ( 'member', 'email@email.com','f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3','MEMBER');
INSERT INTO member(name, email, password,role) VALUES ( 'admin','email2@email.com','f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3','ADMIN');
