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

INSERT INTO reservation_time (start_at) VALUES ('10:00:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00:00');

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-01', 1, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-02', 1, 11);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-03', 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-03', 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-03', 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-03', 4, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-03', 5, 1);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-04', 1, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-04', 2, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-04', 3, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-04', 4, 2);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-05', 1, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-05', 2, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-05', 3, 3);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 4);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 5);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 6);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 7);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 8);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 9);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-06', 1, 10);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-10', 1, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-10', 2, 11);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-11', 2, 12);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('브라운', '2026-05-12', 5, 1);
