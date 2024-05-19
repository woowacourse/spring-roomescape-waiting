SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO member(name, email, password, role) VALUES
('테드', 'test1@email.com', '1450575459', 'USER'),
('종이', 'test1@email.com', '1450575459', 'USER');

INSERT INTO theme (name, description, thumbnail) VALUES
('테마1', '테마1 설명 설명 설명', 'thumbnail1.jpg'),
('테마2', '테마2 설명 설명 설명', 'thumbnail2.jpg');

INSERT INTO reservation_time (start_at) VALUES
('10:00'),
('12:00');

INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES
('2024-05-01', 1, 1, 1),
('2024-05-02', 1, 2, 1),

('2024-05-03', 2, 1, 2),
('2024-05-04', 2, 2, 2);
