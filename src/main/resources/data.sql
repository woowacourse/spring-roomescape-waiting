INSERT INTO RESERVATION_TIME(START_AT)
VALUES ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00');

INSERT INTO THEME(NAME, DESCRIPTION, THUMBNAIL)
VALUES ('테스트1', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테스트2', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테스트3', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테스트4', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO RESERVATION(NAME, DATE, TIME_ID, THEME_ID)
VALUES ('브라운', '2024-04-29', 1, 1),
       ('알유', '2024-04-29', 3, 2),
       ('레디', '2024-04-30', 2, 2),
       ('아임', '2024-05-01', 1, 2),
       ('레디', '2024-05-01', 1, 3),
       ('레디', '2024-05-02', 4, 3);
