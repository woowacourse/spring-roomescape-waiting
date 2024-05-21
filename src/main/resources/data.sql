-- 회원
INSERT INTO MEMBER (NAME, EMAIL, PASSWORD, ROLE)
VALUES ('카키', 'kaki@email.com', '1234', 'USER'),
       ('호기', 'hogi@email.com', '1234', 'ADMIN'),
       ('솔라', 'solar@email.com', '1234', 'USER'),
       ('브라운', 'brown@email.com', '1234', 'USER'),
       ('네오', 'neo@email.com', '1234', 'USER'),
       ('브리', 'bre@email.com', '1234', 'USER'),
       ('포비', 'pobi@email.com', '1234', 'USER'),
       ('구구', 'googoo@email.com', '1234', 'USER'),
       ('토미', 'tomi@email.com', '1234', 'USER'),
       ('리사', 'risa@email.com', '1234', 'USER');
ALTER TABLE MEMBER
    ALTER COLUMN ID RESTART WITH 11;

-- 에약 시간
INSERT INTO RESERVATION_TIME (start_at)
VALUES ('10:00'),
       ('10:30'),
       ('11:00'),
       ('11:30'),
       ('12:00'),
       ('12:30'),
       ('13:00'),
       ('13:30'),
       ('14:00'),
       ('14:30');
ALTER TABLE RESERVATION_TIME
    ALTER COLUMN ID RESTART WITH 11;

-- 테마
INSERT INTO THEME (THEME_NAME, DESCRIPTION, THUMBNAIL)
VALUES ('공포', '무서워요', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('sf', '미래', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('원숭이 사원', '원숭이들의 공격', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('나가야 산다', '빨리 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('좀비 사태', '좀비들의 공격', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('공포의 놀이공원', '놀이공원 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('지하실', '지하실 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('타이타닉', '타이타닉에서 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('미술관을 털어라', '미술관을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('바이러스', '바이러스를 막으세요', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('프리즌 브레이크', '감옥을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('아즈텍 신전', '신전을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우주 정거장', '우주 정거장을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('치과의사', '치과의사를 피해 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('비밀요원', '비밀요원이 돼 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
ALTER TABLE THEME
    ALTER COLUMN ID RESTART WITH 16;

-- 예약
INSERT INTO RESERVATION (MEMBER_ID, DATE, RESERVATION_TIME_ID, THEME_ID, STATUS)
VALUES (1, current_date, 1, 1, 'SUCCESS'),
       (2, current_date, 1, 2, 'SUCCESS'),
       (3, current_date, 1, 3, 'SUCCESS'),
       (4, current_date, 1, 4, 'SUCCESS'),
       (5, current_date, 1, 5, 'SUCCESS'),
       (6, current_date, 1, 6, 'SUCCESS'),
       (7, current_date, 1, 7, 'SUCCESS'),
       (8, current_date, 1, 8, 'SUCCESS'),
       (9, current_date, 1, 9, 'SUCCESS'),
       (1, dateadd('day', 1, current_date), 10, 10, 'SUCCESS'),
       (2, dateadd('day', 1, current_date), 1, 11, 'SUCCESS'),
       (3, dateadd('day', 1, current_date), 2, 12, 'SUCCESS'),
       (4, dateadd('day', 1, current_date), 3, 13, 'SUCCESS'),
       (5, dateadd('day', 1, current_date), 4, 14, 'SUCCESS'),
       (1, dateadd('day', 2, current_date), 5, 15, 'SUCCESS'),
       (2, dateadd('day', 2, current_date), 6, 1, 'SUCCESS'),
       (3, dateadd('day', 2, current_date), 7, 2, 'SUCCESS'),
       (4, dateadd('day', 2, current_date), 8, 3, 'SUCCESS'),
       (1, dateadd('day', 3, current_date), 9, 4, 'SUCCESS'),
       (2, dateadd('day', 3, current_date), 10, 5, 'SUCCESS'),
       (3, dateadd('day', 3, current_date), 1, 6, 'SUCCESS'),
       (1, dateadd('day', 4, current_date), 2, 7, 'SUCCESS'),
       (2, dateadd('day', 4, current_date), 3, 1, 'SUCCESS'),
       (1, dateadd('day', 5, current_date), 4, 1, 'SUCCESS');
ALTER TABLE RESERVATION
    ALTER COLUMN ID RESTART WITH 25;
