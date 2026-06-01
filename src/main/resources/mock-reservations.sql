-- 🥇 1등 (Theme 1: 버려진 정신병원) - 총 10건 (RESERVEWAITING 3건)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-03', 1, 1, 'RESERVED', '2026-05-26 10:00:00');
-- 💡 ★ 대기 순번 검증 슬롯 1 (2026-05-03 / Time 1 / Theme 1)
-- 포비가 이미 ACTIVE인 상태에서 어피치와 네오가 순서대로 대기를 건 상황입니다.
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('어피치', '2026-05-03', 1, 1, 'WAITING', '2026-05-26 10:00:10'); -- 대기 1번 예상
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('네오', '2026-05-03', 1, 1, 'WAITING', '2026-05-26 10:01:15'); -- 대기 2번 예상

INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-05-02', 2, 1, 'RESERVED', '2026-05-26 10:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-05-01', 3, 1, 'RESERVED', '2026-05-26 10:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-04-30', 4, 1, 'RESERVED', '2026-05-26 10:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브라운', '2026-04-29', 5, 1, 'RESERVED', '2026-05-26 10:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('네오', '2026-04-28', 6, 1, 'RESERVED', '2026-05-26 10:25:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('라이언', '2026-04-27', 7, 1, 'RESERVED', '2026-05-26 10:30:00');


-- 🥈 2등 (Theme 7: 오리엔트 특급 살인) - 총 9건 (RESERVED 7건, WAITING 2건)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-02', 3, 7, 'RESERVED', '2026-05-26 11:00:00');
-- 💡 ★ 대기 순번 검증 슬롯 2 (2026-05-02 / Time 3 / Theme 7)
-- 포비가 선점한 자리에 브리인과 제이슨이 순차적으로 대기를 걸었습니다.
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-05-02', 3, 7, 'WAITING', '2026-05-26 11:01:00'); -- 대기 1번 예상
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-05-02', 3, 7, 'WAITING', '2026-05-26 11:02:30'); -- 대기 2번 예상

INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-05-01', 5, 7, 'RESERVED', '2026-05-26 11:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-04-30', 2, 7, 'RESERVED', '2026-05-26 11:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('네오', '2026-04-29', 4, 7, 'RESERVED', '2026-05-26 11:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('라이언', '2026-04-28', 6, 7, 'RESERVED', '2026-05-26 11:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-04-27', 8, 7, 'RESERVED', '2026-05-26 11:25:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브라운', '2026-05-03', 1, 7, 'RESERVED', '2026-05-26 11:30:00');


-- 🥉 3등 (Theme 13: 화성 기지 SOS) - 총 6건 (RESERVED 전원)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('토미', '2026-05-03', 2, 13, 'RESERVED', '2026-05-26 12:00:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제인', '2026-05-02', 4, 13, 'RESERVED', '2026-05-26 12:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-01', 6, 13, 'RESERVED', '2026-05-26 12:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-04-30', 8, 13, 'RESERVED', '2026-05-26 12:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-04-29', 1, 13, 'RESERVED', '2026-05-26 12:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-04-28', 3, 13, 'RESERVED', '2026-05-26 12:25:00');


-- 🎖️ 공동 5등 (Theme 25: 세기의 금고 털기) - 총 5건 (RESERVED 4건, WAITING 1건)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-03', 4, 25, 'RESERVED', '2026-05-26 14:00:00');
-- 💡 ★ 대기 순번 검증 슬롯 3 (2026-05-03 / Time 4 / Theme 25)
-- 포비가 예약한 곳에 리사가 단독 대기를 걸었습니다.
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-05-03', 4, 25, 'WAITING', '2026-05-26 14:00:45'); -- 대기 1번 예상

INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-05-01', 2, 25, 'RESERVED', '2026-05-26 14:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-04-29', 7, 25, 'RESERVED', '2026-05-26 14:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('네오', '2026-04-27', 5, 25, 'RESERVED', '2026-05-26 14:15:00');


-- [이하 단독 예약 데이터 유지]
-- 🎖️ 4등 (Theme 19)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('앨리스', '2026-05-03', 3, 19, 'RESERVED', '2026-05-26 13:00:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('밥', '2026-05-02', 5, 19, 'RESERVED', '2026-05-26 13:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('찰리', '2026-05-01', 7, 19, 'RESERVED', '2026-05-26 13:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('데이비드', '2026-04-30', 1, 19, 'RESERVED', '2026-05-26 13:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('이브', '2026-04-29', 2, 19, 'RESERVED', '2026-05-26 13:20:00');

-- 🎖️ 공동 5등 (Theme 2)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-05-02', 1, 2, 'RESERVED', '2026-05-26 14:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브라운', '2026-04-30', 3, 2, 'RESERVED', '2026-05-26 14:25:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('라이언', '2026-04-28', 5, 2, 'RESERVED', '2026-05-26 14:30:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('어피치', '2026-04-27', 4, 2, 'RESERVED', '2026-05-26 14:35:00');

-- 🎖️ 공동 7등 (Theme 8)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-03', 5, 8, 'RESERVED', '2026-05-26 15:00:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-05-01', 8, 8, 'RESERVED', '2026-05-26 15:05:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-04-29', 6, 8, 'RESERVED', '2026-05-26 15:10:00');

-- 🎖️ 공동 7등 (Theme 14)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브라운', '2026-05-02', 6, 14, 'RESERVED', '2026-05-26 15:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('네오', '2026-04-30', 7, 14, 'RESERVED', '2026-05-26 15:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('라이언', '2026-04-28', 8, 14, 'RESERVED', '2026-05-26 15:25:00');

-- 🎖️ 공동 7등 (Theme 20)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('토미', '2026-05-03', 6, 20, 'RESERVED', '2026-05-26 15:30:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제인', '2026-05-01', 4, 20, 'RESERVED', '2026-05-26 15:35:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('앨리스', '2026-04-29', 3, 20, 'RESERVED', '2026-05-26 15:40:00');

-- 🎖️ 10등 (Theme 26)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('밥', '2026-05-02', 7, 26, 'RESERVED', '2026-05-26 16:00:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('찰리', '2026-04-30', 5, 26, 'RESERVED', '2026-05-26 16:05:00');

-- 엑스트라 데이터 (순위권 밖)
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('포비', '2026-05-03', 7, 3, 'RESERVED', '2026-05-26 16:10:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('리사', '2026-05-02', 8, 9, 'RESERVED', '2026-05-26 16:15:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('브리', '2026-05-01', 1, 15, 'RESERVED', '2026-05-26 16:20:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('제이슨', '2026-04-30', 6, 21, 'RESERVED', '2026-05-26 16:25:00');
INSERT INTO reservation (name, date, time_id, theme_id, status, created_at) VALUES ('라이언', '2026-04-28', 2, 27, 'RESERVED', '2026-05-26 16:30:00');
