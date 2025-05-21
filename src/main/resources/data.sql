INSERT INTO member (name, password, email, role)
VALUES ('엠제이', '1234', 'test1@test.com', 'MEMBER');
INSERT INTO member (name, password, email, role)
VALUES ('저스틴', '1234', 'test2@test.com', 'MEMBER');
INSERT INTO member (name, password, email, role)
VALUES ('리사', '1234', 'test3@test.com', 'MEMBER');
INSERT INTO member (name, password, email, role)
VALUES ('관리자', '1234', 'admin@admin.com', 'ADMIN');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('16:00');
INSERT INTO reservation_time (start_at)
VALUES ('18:00');
INSERT INTO reservation_time (start_at)
VALUES ('20:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('인터스텔라', '시공간을 넘나들며 인류의 미래를 구해야 하는 극한의 두뇌 미션, 인터스텔라 방탈출!',
        'https://upload.wikimedia.org/wikipedia/ko/b/b7/%EC%9D%B8%ED%84%B0%EC%8A%A4%ED%85%94%EB%9D%BC.jpg?20150905075839');
INSERT INTO theme (name, description, thumbnail)
VALUES ('라라랜드', '화려한 꿈과 음악 속에서 단서를 찾아라, 라라랜드 감성의 로맨틱 방탈출!',
        'https://upload.wikimedia.org/wikipedia/ko/8/8f/%EB%9D%BC%EB%9D%BC%EB%9E%9C%EB%93%9C_%ED%8F%AC%EC%8A%A4%ED%84%B0.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('컨저링', '실화 기반의 공포가 현실이 된다, 악령이 도사린 집에서 탈출하라!',
        'https://upload.wikimedia.org/wikipedia/ko/c/c6/%EC%BB%A8%EC%A0%80%EB%A7%81.jpg');

-- 2 -> 1 -> 3
-- 과거 예약
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 2, 2);

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 4, 1, 3);

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 4, 3, 3);

-- 미래 예약
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', +3, CURRENT_DATE), 2, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', +4, CURRENT_DATE), 3, 1, 2);

INSERT INTO waiting (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', +3, CURRENT_DATE), 2, 2, 1);
INSERT INTO waiting (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', +4, CURRENT_DATE), 3, 1, 3);
INSERT INTO waiting (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', +4, CURRENT_DATE), 3, 1, 1);
