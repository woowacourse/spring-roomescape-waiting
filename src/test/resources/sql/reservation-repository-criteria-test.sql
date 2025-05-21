

INSERT INTO member(member_id, name, email, password, role) VALUES
(10000, 'test', 'test@email.com', '1234', 'USER'),
(20000, 'test2', 'test2@email.com', '12345', 'USER');

INSERT INTO reservation_time(time_id, start_at) VALUES
(10000, '10:00'),
(20000, '11:00');

INSERT INTO theme(theme_id, name, description, thumbnail) VALUES
(10000, 'test', 'description', 'thumbnail'),
(20000, 'test2', 'description2', 'thumbnail2');

INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES
(10000, '2025-01-01', 10000, 10000, 'RESERVED'),
(10000, '2025-01-02', 10000, 10000, 'RESERVED'),
(10000, '2025-01-03', 10000, 20000, 'RESERVED'),
(10000, '2025-01-04', 10000, 20000, 'RESERVED'),
(10000, '2025-01-05', 10000, 20000, 'RESERVED'),
(20000, '2025-01-01', 10000, 10000, 'RESERVED'),
(20000, '2025-01-02', 10000, 10000, 'RESERVED'),
(20000, '2025-01-03', 10000, 10000, 'RESERVED'),
(20000, '2025-01-04', 10000, 20000, 'RESERVED'),
(20000, '2025-01-05', 10000, 20000, 'RESERVED');
