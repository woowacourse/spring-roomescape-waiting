-- Reservation times: 10:00 ~ 22:00 (1-hour intervals, IDs 1~13)
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

-- Themes (IDs 1~4)
INSERT INTO theme (name, description, thumbnail_url, amount) VALUES ('공포의 저택', '버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.', 'https://picsum.photos/seed/haunted/400/250', 10000);
INSERT INTO theme (name, description, thumbnail_url, amount) VALUES ('우주 정거장', '고장난 우주 정거장에서 살아남아라. 산소가 30분 후면 바닥난다!', 'https://picsum.photos/seed/spacestation/400/250', 12000);
INSERT INTO theme (name, description, thumbnail_url, amount) VALUES ('마법사의 연구실', '미친 마법사의 연구실에서 탈출하라. 다음 실험 대상이 되기 전에!', 'https://picsum.photos/seed/wizard/400/250', 15000);
INSERT INTO theme (name, description, thumbnail_url, amount) VALUES ('탐정 사무소', '살인 사건의 유일한 용의자가 되었다. 진범을 찾아 무죄를 증명하라.', 'https://picsum.photos/seed/detective/400/250', 11000);

-- Reservations for popular theme ranking
-- 공포의 저택 (theme 1) - 5 bookings
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-04-29', 3, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-06-30', 5, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-01', 7, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-02', 4, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-03', 8, 1);

-- 탐정 사무소 (theme 4) - 4 bookings
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-04-30', 6, 4);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-01', 9, 4);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-02', 11, 4);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-03', 3, 4);

-- 마법사의 연구실 (theme 3) - 3 bookings
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-01', 2, 3);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-04', 6, 3);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-05', 10, 3);

-- 우주 정거장 (theme 2) - 2 bookings
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-03', 4, 2);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-05', 8, 2);

-- Future reservations after 2026-05-26
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-27', 3, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-28', 5, 2);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-29', 7, 4);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-30', 9, 3);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-05-31', 1, 1);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-06-01', 10, 2);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-06-02', 12, 3);
INSERT INTO reservation_slot (date, time_id, theme_id) VALUES ('2026-06-03', 13, 4);

-- Reservations linked to reservation slots
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('김철수', 1, 'RESERVED', '2026-05-27 10:00:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('이영희', 2, 'RESERVED', '2026-05-27 10:01:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('박민준', 3, 'RESERVED', '2026-05-27 10:02:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('최수진', 4, 'RESERVED', '2026-05-27 10:03:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('정다은', 5, 'RESERVED', '2026-05-27 10:04:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('강현수', 6, 'RESERVED', '2026-05-27 10:05:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('윤지원', 7, 'RESERVED', '2026-05-27 10:06:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('임서준', 8, 'RESERVED', '2026-05-27 10:07:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('한지아', 9, 'RESERVED', '2026-05-27 10:08:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('송민재', 10, 'RESERVED', '2026-05-27 10:09:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('오하린', 11, 'RESERVED', '2026-05-27 10:10:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('문서윤', 12, 'RESERVED', '2026-05-27 10:11:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('배준호', 13, 'RESERVED', '2026-05-27 10:12:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('서지우', 14, 'RESERVED', '2026-05-27 10:13:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('남도윤', 15, 'RESERVED', '2026-05-27 10:14:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('권예린', 16, 'RESERVED', '2026-05-27 10:15:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('조민성', 17, 'RESERVED', '2026-05-27 10:16:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('안유진', 18, 'RESERVED', '2026-05-27 10:17:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('신하늘', 19, 'RESERVED', '2026-05-27 10:18:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('유지호', 20, 'RESERVED', '2026-05-27 10:19:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('홍서연', 21, 'RESERVED', '2026-05-27 10:20:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('백시우', 22, 'RESERVED', '2026-05-27 10:21:00');
INSERT INTO reservation (name, reservation_slot_id, status, updated_at) VALUES ('과거대기', 1, 'RESERVED', '2026-05-27 10:22:00');
