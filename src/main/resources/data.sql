INSERT INTO theme(name, description, thumbnail)
VALUES ('테마1', '테마1입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg'),
       ('테마2', '테마2입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg'),
       ('테마3', '테마3입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

INSERT INTO reservation_time(start_at)
VALUES ('15:00:00'),
       ('16:00:00'),
       ('17:00:00');

INSERT INTO member(name, email, password, role)
VALUES ('썬', 'sun@test.com', '123', 'MEMBER'),
       ('리비', 'libienz@test.com', '123', 'MEMBER'),
       ('도도', 'dodo@test.com', '123', 'MEMBER'),
       ('어드민', 'admin@test.com', '123', 'ADMIN');


INSERT INTO reservation(member_id, date, time_id, theme_id)
VALUES (1, TIMESTAMPADD(DAY, -1, NOW()), '1', '1'),
       (2, TIMESTAMPADD(DAY, -1, NOW()), '2', '1'),
       (1, TIMESTAMPADD(DAY, -1, NOW()), '3', '1'),
       (3, TIMESTAMPADD(DAY, -2, NOW()), '1', '2'),
       (2, TIMESTAMPADD(DAY, -2, NOW()), '2', '2');


