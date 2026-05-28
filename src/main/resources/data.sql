-- 테마
INSERT INTO theme (name, description, url) VALUES ('우테코 공포물', '레벨2 미션의 공포', '/images/horror');
INSERT INTO theme (name, description, url) VALUES ('미래 도시', '2050년 서울의 이야기', '/images/future-city');
INSERT INTO theme (name, description, url) VALUES ('고대 이집트', '파라오의 저주를 풀어라', '/images/egypt');
INSERT INTO theme (name, description, url) VALUES ('우주 탐험', '블랙홀 너머의 세계', '/images/space');
INSERT INTO theme (name, description, url) VALUES ('마법 학교', '마법사가 되기 위한 여정', '/images/magic-school');
INSERT INTO theme (name, description, url) VALUES ('해저 왕국', '심해 속 숨겨진 비밀', '/images/underwater');
INSERT INTO theme (name, description, url) VALUES ('좀비 아포칼립스', '살아남아야 한다', '/images/zombie');
INSERT INTO theme (name, description, url) VALUES ('탐정 사무소', '미해결 사건의 진실', '/images/detective');
INSERT INTO theme (name, description, url) VALUES ('시간 여행', '과거로 돌아가 역사를 바꿔라', '/images/time-travel');
INSERT INTO theme (name, description, url) VALUES ('서부 개척시대', '황야의 무법자를 잡아라', '/images/western');
INSERT INTO theme (name, description, url) VALUES ('저주받은 저택', '100년 전 사라진 가문의 비밀을 파헤쳐라', '/images/cursed-mansion');
INSERT INTO theme (name, description, url) VALUES ('심해 탈출', '침몰하는 잠수함, 당신에게 남은 시간은 60분', '/images/deep-sea');
INSERT INTO theme (name, description, url) VALUES ('폐병원의 진실', '1978년 이후 아무도 돌아오지 못한 그곳', '/images/abandoned-hospital');
INSERT INTO theme (name, description, url) VALUES ('마지막 탐정', '연쇄 살인마의 다음 타깃은 바로 당신이다', '/images/last-detective');
INSERT INTO theme (name, description, url) VALUES ('시간의 틈', '시공간이 뒤틀린 고대 유적 속에 갇혀버렸다', '/images/time-rift');

-- 예약 시간
INSERT INTO reservation_time (start_at) VALUES ('10:00:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00:00');

-- 과거 7일(2026-05-21 ~ 2026-05-27) 예약 — 인기 테마 집계용
-- 저주받은 저택(11)을 최다, 심해 탈출(12) 다음 순으로 노출
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-21', 1, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-21', 2, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-21', 3, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-22', 1, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-22', 2, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-23', 1, 11);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-21', 4, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-22', 4, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-23', 4, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-24', 4, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-25', 4, 12);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-22', 5, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-23', 5, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-24', 5, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-25', 5, 1);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-23', 6, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-24', 6, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-25', 6, 3);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-24', 7, 5);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-25', 7, 5);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-26', 1, 4);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-26', 2, 4);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-05-27', 3, 8);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-27', 4, 14);

-- 미래 예약 — 사용자 화면 데모용
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-29', 1, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-29', 2, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-30', 5, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-05-30', 6, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('토리', '2026-06-01', 3, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('포비', '2026-06-02', 4, 5);

-- 예약 대기 — 같은 슬롯에 대해 토리/포비가 순번을 기다리는 시나리오
INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES ('토리', '2026-05-29', 1, 11, '2026-05-27T10:00');
INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES ('포비', '2026-05-29', 1, 11, '2026-05-27T11:30');
INSERT INTO waiting (name, date, time_id, theme_id, created_at) VALUES ('포비', '2026-05-30', 5, 12, '2026-05-28T09:00');