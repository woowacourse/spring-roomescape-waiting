INSERT INTO RESERVATION_TIME (START_AT)
VALUES ('10:15'),
       ('11:20'),
       ('12:25');

INSERT INTO THEME (NAME, DESCRIPTION, THUMBNAIL)
VALUES ('spring', 'Escape from spring cold',
        'https://img.freepik.com/free-photo/spring-landscape-with-tulips-daisies_123827-29597.jpg?size=626&ext=jpg&ga=GA1.1.553209589.1714348800&semt=sph'),
       ('summer', 'Escape from hottest weather',
        'https://www.lifewire.com/thmb/unRdFjROthGxGQXL9SZIbc5BzeA=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/summerbeach-5b4650c946e0fb005bfb3207.jpg'),
       ('autumn', 'Escape from autumn',
        'https://en.wikipedia.org/wiki/File:Fall_(2022_film).jpg'),
       ('winter', 'Escape from cold',
        'https://img.youtube.com/vi/R3eHHBXPJqI/hqdefault.jpg');

INSERT INTO RESERVATION (NAME, DATE, TIME_ID, THEME_ID)
VALUES ('al', CURRENT_DATE - 1, 1, 1),
       ('be', CURRENT_DATE - 2, 2, 2);
