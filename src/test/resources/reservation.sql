INSERT INTO member (id, email, password, name, role)
VALUES (1, 'alstn1@gmail.com', '1234', '구름a', 'USER'),
       (2, 'alstn2@gmail.com', '1234', '구름b', 'USER');

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, '고풍 한옥 마을', '한국의 전통적인 아름다움이 당신을 맞이합니다.', 'https://via.placeholder.com/150/92c952'),
       (2, '우주 탐험', '끝없는 우주에 숨겨진 비밀을 파헤치세요.', 'https://via.placeholder.com/150/771796'),
       (3, '시간여행', '과거와 미래를 오가며 역사의 비밀을 밝혀보세요.', 'https://via.placeholder.com/150/24f355'),
       (4, '마법의 숲', '요정과 마법사들이 사는 신비로운 숲 속으로!', 'https://via.placeholder.com/150/30f9e7');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '09:00'),
       (2, '12:00'),
       (3, '17:00'),
       (4, '21:00');

INSERT INTO reservation (id, date, member_id, time_id, theme_id)
VALUES (1, '2024-04-08', 1, 1, 1),
       (4, '2024-04-08', 1, 2, 2),
       (5, '2024-04-09', 2, 2, 1),
       (6, '2024-04-09', 1, 2, 2),
       (7, '2024-04-09', 2, 3, 3),
       (8, '2024-04-09', 1, 4, 1),
       (9, '2024-04-10', 1, 1, 4),
       (10, '2024-04-10', 2, 2, 4);
