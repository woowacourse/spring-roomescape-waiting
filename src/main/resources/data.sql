INSERT INTO reservation_time (id, start_at) VALUES
(1, '10:00:00'),
(2, '11:30:00'),
(3, '13:00:00'),
(4, '15:00:00'),
(5, '16:30:00'),
(6, '18:00:00');

INSERT INTO theme (id, name, description, thumbnail_url) VALUES
(1, '사라진 기록보관소', '폐관 직전의 오래된 도서관에서 사라진 원고와 마지막 열쇠를 추적하는 미스터리 테마입니다.', 'https://commons.wikimedia.org/wiki/Special:FilePath/Main%20%28Reading%20Room%29%20%283109281787%29.jpg?width=1200'),
(2, '13호실의 체크아웃', '불 꺼진 호텔 복도와 잠긴 객실 사이에서 투숙객의 마지막 동선을 복원해야 합니다.', 'https://commons.wikimedia.org/wiki/Special:FilePath/Hotel%20Hallway.jpeg?width=1200'),
(3, '제로 랩', '폐쇄된 연구실에 남은 실험 기록을 해독하고 제한 시간 안에 격리 구역을 빠져나가야 합니다.', 'https://commons.wikimedia.org/wiki/Special:FilePath/Room%20405%2C%20George%20Herbert%20Jones%20Laboratory%2C%20The%20University%20of%20Chicago%20%287189830229%29.jpg?width=1200'),
(4, '지하 벙커 113', '냉전 시대의 지하 벙커에서 비상 전력과 암호 장치를 복구하는 긴장감 높은 탈출극입니다.', 'https://commons.wikimedia.org/wiki/Special:FilePath/Bunker%20113%20Bernbach.jpg?width=1200'),
(5, '심야열차 마지막 칸', '새벽에 멈춰 선 열차 객실에서 남겨진 승차권과 수하물 단서를 따라 사건을 풀어야 합니다.', 'https://commons.wikimedia.org/wiki/Special:FilePath/Passenger%20Compartment%20on%20a%20train%28GN04216%29.jpg?width=1200');

INSERT INTO reservation (id, name, date, time_id, theme_id, created_at) VALUES
(1, '강민준', CURRENT_DATE, 1, 1, CURRENT_TIMESTAMP),
(2, '이서연', CURRENT_DATE, 2, 2, CURRENT_TIMESTAMP),
(3, '박도윤', CURRENT_DATE, 3, 3, CURRENT_TIMESTAMP),
(4, '최하린', DATEADD('DAY', 1, CURRENT_DATE), 4, 4, CURRENT_TIMESTAMP),
(5, '정우진', DATEADD('DAY', 1, CURRENT_DATE), 5, 5, CURRENT_TIMESTAMP);

ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 7;
ALTER TABLE theme ALTER COLUMN id RESTART WITH 6;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 6;
