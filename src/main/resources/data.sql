-- Reservation times: 10:00 ~ 22:00
INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('19:00');
INSERT INTO reservation_time (start_at) VALUES ('20:00');
INSERT INTO reservation_time (start_at) VALUES ('21:00');
INSERT INTO reservation_time (start_at) VALUES ('22:00');

-- Themes
INSERT INTO theme (name, description, thumbnail_url, price)
VALUES ('공포의 저택', '버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.', 'https://picsum.photos/seed/haunted/400/250', 22000);
INSERT INTO theme (name, description, thumbnail_url, price)
VALUES ('우주 정거장', '고장난 우주 정거장에서 살아남아라. 산소가 30분 후면 바닥난다!', 'https://picsum.photos/seed/spacestation/400/250', 25000);
INSERT INTO theme (name, description, thumbnail_url, price)
VALUES ('마법사의 연구실', '미친 마법사의 연구실에서 탈출하라. 다음 실험 대상이 되기 전에!', 'https://picsum.photos/seed/wizard/400/250', 23000);
INSERT INTO theme (name, description, thumbnail_url, price)
VALUES ('탐정 사무소', '살인 사건의 유일한 용의자가 되었다. 진범을 찾아 무죄를 증명하라.', 'https://picsum.photos/seed/detective/400/250', 21000);

-- Schedules
-- Past schedules: 조회 시 RESERVED는 COMPLETED, WAITING은 EXPIRED로 표시된다.
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-05-28', 1, 1);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-05-29', 2, 2);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-05-31', 3, 3);

-- Future schedules
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-02', 1, 1);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-03', 5, 2);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-04', 6, 3);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-05', 7, 4);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-06', 8, 1);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-07', 9, 2);
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-06-08', 10, 3);

-- schedule_id: 11
INSERT INTO schedule (date, time_id, theme_id) VALUES ('2026-07-02', 10, 3);

-- Users
INSERT INTO users (login_id, name, password, role)
VALUES ('ruro', '러로', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('brownRice', '현미밥', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('ottogiRice', '오뚜기밥', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('mixedRice', '잡곡밥', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('blackRice', '흑미밥', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('whiteRice', '흰쌀밥', '1234', 'USER');
INSERT INTO users (login_id, name, password, role)
VALUES ('admin', '관리자', '1234', 'ADMIN');

-- Reservations
-- Past: already finished or expired. These cannot be changed or canceled by current domain rules.
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (1, 1, 'RESERVED', '2026-05-20 09:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (2, 2, 'WAITING', '2026-05-21 09:30:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (3, 3, 'CANCELED', '2026-05-30 18:00:00');

-- 2026-06-02 공포의 저택 10:00: one confirmed reservation, two waiting reservations, one canceled history.
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (1, 4, 'RESERVED', '2026-05-31 09:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (2, 4, 'WAITING', '2026-05-31 09:05:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (3, 4, 'WAITING', '2026-05-31 09:10:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (4, 4, 'CANCELED', '2026-05-31 09:15:00');

-- 2026-06-03 우주 정거장 14:00: waiting order can be checked clearly.
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (4, 5, 'RESERVED', '2026-05-31 10:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (5, 5, 'WAITING', '2026-05-31 10:05:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (6, 5, 'WAITING', '2026-05-31 10:10:00');

-- Other future schedules for admin/user screens.
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (6, 6, 'RESERVED', '2026-05-31 11:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (1, 6, 'CANCELED', '2026-05-31 11:20:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (2, 7, 'RESERVED', '2026-05-31 12:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (3, 8, 'RESERVED', '2026-05-31 13:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (5, 9, 'CANCELED', '2026-05-31 14:00:00');



INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (1, 11, 'RESERVED', '2026-05-31 14:00:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (2, 11, 'WAITING', '2026-05-31 14:01:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (3, 11, 'WAITING', '2026-05-31 14:02:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (4, 11, 'WAITING', '2026-05-31 14:03:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (5, 11, 'WAITING', '2026-05-31 14:04:00');
INSERT INTO reservation (user_id, schedule_id, status, updated_at)
VALUES (6, 11, 'WAITING', '2026-05-31 14:05:00');
