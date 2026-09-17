-- 1. 테마(Theme) 20개 삽입
INSERT INTO theme (name, description, thumbnail_url) VALUES
                                                         ('폐병원 탈출', '버려진 병원에서 벌어지는 기괴한 일들. 살아서 나가야 한다.', 'https://example.com/t1.jpg'),
                                                         ('비밀요원 Z', '적국의 핵미사일 발사를 막아라!', 'https://example.com/t2.jpg'),
                                                         ('마법사의 서재', '전설의 마법사가 숨겨둔 비밀의 책을 찾아라.', 'https://example.com/t3.jpg'),
                                                         ('우주 정거장 조난', '산소가 떨어지기 전 구명정을 타야 한다.', 'https://example.com/t4.jpg'),
                                                         ('오리엔트 특급 살인', '열차 안에서 벌어진 밀실 살인 사건의 범인은?', 'https://example.com/t5.jpg'),
                                                         ('파라오의 무덤', '수천 년간 잠들어 있던 저주를 풀고 탈출하라.', 'https://example.com/t6.jpg'),
                                                         ('뱀파이어 백작의 성', '어둠이 내리면 깨어나는 백작을 피해 도망쳐라.', 'https://example.com/t7.jpg'),
                                                         ('해적선 블랙펄', '유령 선원들이 지키는 보물을 훔쳐 달아나라.', 'https://example.com/t8.jpg'),
                                                         ('좀비 바이러스 연구소', '백신을 찾지 못하면 당신도 감염된다.', 'https://example.com/t9.jpg'),
                                                         ('셜록 홈즈: 마지막 사건', '모리어티 교수가 파놓은 함정에서 벗어나라.', 'https://example.com/t10.jpg'),
                                                         ('앨리스의 기묘한 다과회', '미치광이 모자장수의 퍼즐을 풀어야 탈출할 수 있다.', 'https://example.com/t11.jpg'),
                                                         ('사이버펑크 2077', '네온사인 아래 숨겨진 거대 기업의 음모를 파헤쳐라.', 'https://example.com/t12.jpg'),
                                                         ('심해 탐험 아틀란티스', '산소 탱크가 비어간다. 전설의 대륙에서 빠져나가라.', 'https://example.com/t13.jpg'),
                                                         ('아마존 밀림 조난', '맹수와 원주민의 위협 속에서 구조대를 기다려라.', 'https://example.com/t14.jpg'),
                                                         ('엑소시스트: 악령 퇴치', '소녀의 몸에 깃든 악마의 진짜 이름을 알아내라.', 'https://example.com/t15.jpg'),
                                                         ('시간 여행자의 시계', '과거를 바꿔버리면 현재의 당신이 소멸한다.', 'https://example.com/t16.jpg'),
                                                         ('닌자 마을의 비밀', '적진 한가운데서 기밀 문서를 탈취하라.', 'https://example.com/t17.jpg'),
                                                         ('인공지능의 반란', '시설의 통제권을 빼앗은 메인프레임을 셧다운하라.', 'https://example.com/t18.jpg'),
                                                         ('드라큘라의 만찬', '초대받지 않은 손님은 메인 요리가 될 뿐이다.', 'https://example.com/t19.jpg'),
                                                         ('무인도 생존기', '가진 것은 오직 성냥 한 개피. 탈출선을 만들어라.', 'https://example.com/t20.jpg');

-- 2. 예약 시간(Time) 14개 삽입
INSERT INTO time (start_at) VALUES
                                ('09:00:00'), ('10:00:00'), ('11:00:00'), ('12:00:00'), ('13:00:00'), ('14:00:00'), ('15:00:00'),
                                ('16:00:00'), ('17:00:00'), ('18:00:00'), ('19:00:00'), ('20:00:00'), ('21:00:00'), ('22:00:00');

-- 3. 예약 seed 삽입 (theme_slot 생성 후 reservation.theme_slot_id로 연결)
CREATE TABLE reservation_seed
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    status   VARCHAR(255) NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    PRIMARY KEY (id)
);


-- [Theme 1: 폐병원 탈출]
INSERT INTO reservation_seed (name, date, status, time_id, theme_id) VALUES
                                                                    ('게스트', '2026-05-07', 'CONFIRMED', 1, 1), ('게스트', '2026-05-07', 'CONFIRMED', 4, 1), ('게스트', '2026-05-07', 'CONFIRMED', 8, 1), ('게스트', '2026-05-08', 'CONFIRMED', 2, 1), ('게스트', '2026-05-08', 'CONFIRMED', 6, 1), ('게스트', '2026-05-08', 'CONFIRMED', 10, 1),
                                                                    ('게스트', '2026-05-09', 'CONFIRMED', 1, 1), ('게스트', '2026-05-09', 'CONFIRMED', 3, 1), ('게스트', '2026-05-09', 'CONFIRMED', 5, 1), ('게스트', '2026-05-10', 'CONFIRMED', 7, 1), ('게스트', '2026-05-10', 'CONFIRMED', 9, 1), ('게스트', '2026-05-10', 'CONFIRMED', 12, 1),
                                                                    ('게스트', '2026-05-12', 'CONFIRMED', 2, 1), ('게스트', '2026-05-12', 'CONFIRMED', 6, 1), ('게스트', '2026-05-12', 'CONFIRMED', 11, 1), ('게스트', '2026-05-14', 'CONFIRMED', 1, 1), ('게스트', '2026-05-14', 'CONFIRMED', 4, 1), ('게스트', '2026-05-14', 'CONFIRMED', 8, 1),
                                                                    ('게스트', '2026-05-30', 'CONFIRMED', 3, 1), ('게스트', '2026-05-16', 'CONFIRMED', 7, 1), ('게스트', '2026-05-16', 'CONFIRMED', 10, 1), ('게스트', '2026-05-18', 'CONFIRMED', 2, 1), ('게스트', '2026-05-18', 'CONFIRMED', 5, 1), ('게스트', '2026-05-18', 'CONFIRMED', 9, 1),
                                                                    ('게스트', '2026-05-20', 'CONFIRMED', 1, 1), ('게스트', '2026-05-20', 'CONFIRMED', 6, 1), ('게스트', '2026-05-20', 'CONFIRMED', 12, 1), ('게스트', '2026-05-22', 'CONFIRMED', 4, 1), ('게스트', '2026-05-22', 'CONFIRMED', 8, 1), ('게스트', '2026-05-22', 'CONFIRMED', 11, 1),
                                                                    ('게스트', '2026-05-25', 'CONFIRMED', 2, 1), ('게스트', '2026-05-25', 'CONFIRMED', 5, 1), ('게스트', '2026-05-25', 'CONFIRMED', 10, 1), ('게스트', '2028-05-27', 'CONFIRMED', 3, 1), ('게스트', '2026-05-27', 'CONFIRMED', 7, 1), ('게스트', '2026-05-27', 'CONFIRMED', 9, 1),
                                                                    ('게스트', '2026-05-29', 'CONFIRMED', 1, 1), ('게스트', '2026-05-29', 'CONFIRMED', 6, 1), ('게스트', '2026-05-31', 'CONFIRMED', 4, 1), ('게스트', '2026-05-31', 'CONFIRMED', 12, 1);

-- [Theme 2: 비밀요원 Z]
INSERT INTO reservation_seed (name, date, status, time_id, theme_id) VALUES
                                                                    ('게스트', '2026-05-07', 'CONFIRMED', 2, 2), ('게스트', '2026-05-07', 'CONFIRMED', 5, 2), ('게스트', '2026-05-08', 'CONFIRMED', 1, 2), ('게스트', '2026-05-08', 'CONFIRMED', 7, 2), ('게스트', '2026-05-09', 'CONFIRMED', 2, 2), ('게스트', '2026-05-09', 'CONFIRMED', 8, 2),
                                                                    ('게스트', '2026-05-11', 'CONFIRMED', 3, 2), ('게스트', '2026-05-11', 'CONFIRMED', 6, 2), ('게스트', '2026-05-13', 'CONFIRMED', 4, 2), ('게스트', '2026-05-13', 'CONFIRMED', 9, 2), ('게스트', '2026-05-14', 'CONFIRMED', 2, 2), ('게스트', '2026-05-14', 'CONFIRMED', 10, 2),
                                                                    ('게스트', '2026-05-15', 'CONFIRMED', 5, 2), ('게스트', '2026-05-15', 'CONFIRMED', 11, 2), ('게스트', '2026-05-17', 'CONFIRMED', 1, 2), ('게스트', '2026-05-17', 'CONFIRMED', 6, 2), ('게스트', '2026-05-19', 'CONFIRMED', 3, 2), ('게스트', '2026-05-19', 'CONFIRMED', 8, 2),
                                                                    ('게스트', '2026-05-21', 'CONFIRMED', 4, 2), ('게스트', '2026-05-21', 'CONFIRMED', 9, 2), ('게스트', '2026-05-23', 'CONFIRMED', 2, 2), ('게스트', '2026-05-23', 'CONFIRMED', 10, 2), ('게스트', '2026-05-24', 'CONFIRMED', 5, 2), ('게스트', '2026-05-24', 'CONFIRMED', 12, 2),
                                                                    ('게스트', '2026-05-26', 'CONFIRMED', 1, 2), ('게스트', '2026-05-26', 'CONFIRMED', 7, 2), ('게스트', '2026-05-28', 'CONFIRMED', 3, 2), ('게스트', '2026-05-28', 'CONFIRMED', 8, 2), ('게스트', '2026-05-28', 'CONFIRMED', 11, 2), ('게스트', '2026-05-30', 'CONFIRMED', 4, 2),
                                                                    ('게스트', '2026-05-30', 'CONFIRMED', 9, 2), ('게스트', '2026-05-30', 'CONFIRMED', 13, 2), ('게스트', '2026-05-31', 'CONFIRMED', 2, 2), ('게스트', '2026-05-31', 'CONFIRMED', 6, 2), ('게스트', '2026-05-31', 'CONFIRMED', 10, 2);

-- [과거 데이터]
INSERT INTO reservation_seed (name, date, status, time_id, theme_id) VALUES
                                                                    ('과거게스트', '2026-04-26', 'COMPLETED', 1, 1), ('과거게스트', '2026-04-26', 'COMPLETED', 4, 1), ('과거게스트', '2026-04-26', 'CANCELLED', 7, 2);

-- [Theme 3: 마법사의 서재 — 예약 대기(PENDING) 시연용]
-- 한 슬롯에 첫 예약자가 CONFIRMED로 자리를 잡고, 뒤이은 신청자들이 삽입 순서(=id)대로 PENDING 대기 줄을 선다.
-- 대기 순번은 reservation.id 오름차순으로 부여되므로, 아래 INSERT 순서가 곧 대기 순번이 된다.
INSERT INTO reservation_seed (name, date, status, time_id, theme_id) VALUES
                                                                    -- 슬롯 A (2026-05-28 13:00): 브라운 확정 → 네오(대기 1) → 포비(대기 2)
                                                                    ('브라운', '2026-05-28', 'CONFIRMED', 5, 3),
                                                                    ('네오', '2026-05-28', 'PENDING', 5, 3),
                                                                    ('포비', '2026-05-28', 'PENDING', 5, 3),
                                                                    -- 슬롯 B (2026-05-29 15:00): 네오 확정 → 브라운(대기 1) → 제이슨(대기 2)
                                                                    ('네오', '2026-05-29', 'CONFIRMED', 7, 3),
                                                                    ('브라운', '2026-05-29', 'PENDING', 7, 3),
                                                                    ('제이슨', '2026-05-29', 'PENDING', 7, 3),
                                                                    -- 브라운 과거 이력: 완료 1건, 취소 1건 (내 예약 조회에서 모든 상태가 보이도록)
                                                                    ('브라운', '2026-05-20', 'COMPLETED', 3, 3),
                                                                    ('브라운', '2026-05-22', 'CANCELLED', 4, 3);

-- [Theme 4: 우주 정거장 조난 — 추가 대기 시연]
INSERT INTO reservation_seed (name, date, status, time_id, theme_id) VALUES
                                                                    -- 슬롯 C (2026-05-30 14:00): 포비 확정 → 브라운(대기 1)
                                                                    ('포비', '2026-05-30', 'CONFIRMED', 6, 4),
                                                                    ('브라운', '2026-05-30', 'PENDING', 6, 4);

-- 4. 테마 슬롯(Theme_Slot) 생성 후 예약 연결
INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
SELECT
    theme_id,
    date,
    time_id,
    CASE
        WHEN SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) > 0 THEN TRUE
        ELSE FALSE
    END
FROM reservation_seed
GROUP BY theme_id, date, time_id
ORDER BY MIN(id);

INSERT INTO reservation (name, status, theme_slot_id)
SELECT
    rs.name,
    rs.status,
    ts.id
FROM reservation_seed rs
         INNER JOIN theme_slot ts
                    ON ts.theme_id = rs.theme_id
                        AND ts.date = rs.date
                        AND ts.time_id = rs.time_id
WHERE rs.status != 'PENDING'
ORDER BY rs.id;

INSERT INTO waiting (member_name, date, time_id, theme_id)
SELECT
    name,
    date,
    time_id,
    theme_id
FROM reservation_seed
WHERE status = 'PENDING'
ORDER BY id;

DROP TABLE reservation_seed;
