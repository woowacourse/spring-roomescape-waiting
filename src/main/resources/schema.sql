-- [1단계 메모] theme · reservation_time 은 @Entity 로 매핑됐으나, reservation · waiting 이 아직
-- 비-JPA(@JdbcTest 슬라이스)라 schema.sql 을 전체 DDL 권위로 유지한다(ddl-auto=none).
-- Hibernate 자동 생성 DDL 은 격리 관찰로 기록(02 문서). 4개 테이블이 모두 엔티티가 되는 1-2 종료 시 create-drop 으로 전환.

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
