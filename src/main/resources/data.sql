-- 테마 등록 (id 자동 증가)
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 저택', '무서운 이야기', 'url1'),
       ('미스터리 학교', '괴담과 미스터리', 'url2'),
       ('마법사의 방', '판타지 테마', 'url3'),
       ('우주선 탈출', 'SF 탈출', 'url4'),
       ('탐정 사무소', '추리게임', 'url5'),
       ('사라진 유물', '고대 유물 추적', 'url6'),
       ('지하 감옥', '공포감 넘치는 감옥', 'url7'),
       ('해적의 보물', '보물찾기', 'url8'),
       ('유령 열차', '소름돋는 열차 여행', 'url9'),
       ('저주받은 인형', '인형의 저주', 'url10');

-- 예약 시간 등록 (id 자동 증가)
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('14:00'),
       ('18:00');

INSERT INTO member (name, email, password, role)
VALUES ('히로', 'example@gmail.com', 'password', 'ADMIN');
