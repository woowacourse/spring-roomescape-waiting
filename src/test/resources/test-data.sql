MERGE INTO reservation_time(id, start_at) KEY (id) VALUES
    (1, '10:00'),
    (2, '11:00'),
    (3, '12:00'),
    (4, '13:00'),
    (5, '14:00'),
    (6, '15:00');

MERGE INTO theme(id, name, description, thumbnail) KEY (id) VALUES
    (1, '테스트 테마1', '테마 설명1', '썸네일 주소1'),
    (2, '테스트 테마2', '테마 설명2', '썸네일 주소2');
