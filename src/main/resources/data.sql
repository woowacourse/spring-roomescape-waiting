-- Theme (방탈출 테마)
INSERT INTO theme (name, description, thumbnail)
VALUES ('심해의 공포', 'description', 'https://images.unsplash.com/photo-1564866651-59457d9f3e0e'),
       ('미로의 저주', '끝없는 미로에서 탈출하라', 'https://images.pexels.com/photos/1113447/pexels-photo-1113447.jpeg'),
       ('시간여행자', '과거와 미래를 넘나드는 테마', 'https://cdn.pixabay.com/photo/2021/06/16/06/01/mountains-6336949_960_720.jpg');

-- Reservation Time
INSERT INTO reservation_time (start_at)
VALUES ('12:00:00'),
       ('14:00:00'),
       ('16:00:00');

-- Member (회원 + 이메일, 비밀번호 포함)
INSERT INTO member (name, email, role, password)
VALUES ('히스타', 'hista@example.com', 'USER', 'password1'),
       ('권건형', 'rjsgud19566@gmail.com', 'USER', '123'),
       ('관리자', 'rjsgud1956@naver.com', 'ADMIN', '123'),
       ('아리아', 'aria@example.com', 'USER', 'password3');

-- Reservation (10건)
INSERT INTO reservation (date, member_id, theme_id, time_id)
VALUES ('2025-05-01', 1, 1, 1),
       ('2025-05-02', 1, 2, 1),
       ('2025-05-03', 1, 3, 1),
       ('2025-05-04', 1, 1, 1),
       ('2025-05-01', 2, 2, 2),
       ('2025-05-10', 2, 3, 2),
       ('2025-05-9', 2, 1, 2),
       ('2025-05-08', 3, 3, 3),
       ('2025-05-07', 3, 1, 3),
       ('2025-05-03', 3, 2, 3);
