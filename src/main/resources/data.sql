INSERT INTO RESERVATION_TIME(START_AT)
VALUES ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00'),
       ('19:00');

INSERT INTO THEME(NAME, DESCRIPTION, THUMBNAIL)
VALUES ('봄', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('여름', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('가을', '설명3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('겨울', '설명4', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO MEMBER(NAME, EMAIL, PASSWORD, ROLE)
VALUES ('레디', 'redddy@gmail.com', '3e2b2d79b2f6f90ba2f3ae18a90ae990b149b2fdbc208f55ac9b763b4dad0a16', 'ADMIN'),
       ('재즈', 'gkatjraud1@redddybabo.com', 'f8e5bc8fdaca1bccab597398b0f26a814c1486fb8a37515c075f4ff023abe726', 'USER'),
       ('제제', 'jinwuo0925@gmail.com', '4e5ef62cc65465fbd14118b6e894aa0fa3fbe72c31fcdb6829a3ea21163b3e3a', 'USER');

INSERT INTO RESERVATION(MEMBER_ID, DATE, TIME_ID, THEME_ID)
VALUES (1, CURRENT_DATE - 3, 1, 1),
       (2, CURRENT_DATE - 2, 3, 2),
       (1, CURRENT_DATE - 1, 2, 2),
       (2, CURRENT_DATE - 1, 1, 2),
       (3, CURRENT_DATE - 7, 1, 3),
       (3, CURRENT_DATE + 3, 4, 3);
