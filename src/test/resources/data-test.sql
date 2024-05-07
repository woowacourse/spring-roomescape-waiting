SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE registration RESTART IDENTITY;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO reservation_time (start_at)
VALUES ('15:40'),
       ('13:40'),
       ('17:40');

INSERT INTO theme (name, description, thumbnail)
VALUES ('polla', '폴라 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('dobby', '도비 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('pobi', '포비 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO registration (name, email, password, role)
VALUES ('어드민', 'admin@admin.com', '1234', 'ADMIN'),
       ('어드민2', 'admin2@admin.com', '1234', 'ADMIN');

INSERT INTO registration (name, email, password)
VALUES ('도비', 'kimdobby@wootaeco.com', 'pass1'),
       ('피케이', 'pke@best.com', 'pass2'),
       ('테스트', 'test@test.com', 'test');

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('도비', CURRENT_DATE(), 1, 1),
       ('피케이', CURRENT_DATE(), 2, 1);
