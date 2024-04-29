DELETE FROM reservation;
ALTER TABLE reservation ALTER COLUMN id RESTART;

DELETE  FROM reservation_time;
ALTER TABLE reservation_time ALTER COLUMN id RESTART;

DELETE  FROM theme;
ALTER TABLE theme ALTER COLUMN id RESTART;

DELETE  FROM member;
ALTER TABLE member ALTER COLUMN id RESTART;


INSERT INTO member(name, email, password, role)
VALUES ('썬', 'aa@gmail.com', '123', 'MEMBER'),
       ('리비', 'bb@gmail.com', '123', 'MEMBER'),
       ('도도', 'cc@gmail.com', '123', 'MEMBER'),
       ('어드민', 'dd@gmail.com', '123', 'ADMIN');


INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('잠실 캠퍼스 탈출',
        '미션을 빨리 진행하고 잠실 캠퍼스를 탈출하자!',
        'https://velog.velcdn.com/images/jangws/post/cfe0e548-1242-470d-bfa8-19eeb72debc5/image.jpg'),
       ('선릉 캠퍼스 탈출',
        '미션을 빨리 진행하고 선릉 캠퍼스를 탈출하자!',
        'https://techblog.woowahan.com/wp-content/uploads/2022/06/iOS-%EC%9D%B4%EB%AF%B8%EC%A7%80.jpg'),
        ('강박 탈출',
        '잘해야 한다는 강박으로부터 탈출하자!',
        'https://images.velog.io/images/jhy979/post/2e3aad39-3beb-47f9-849b-71b5cb426dc2/%EC%9A%B0%ED%85%8C%EC%BD%94.png');

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, TIMESTAMPADD(DAY,1, NOW()), 1, 1),
       (2, TIMESTAMPADD(DAY,1, NOW()), 2, 1),
       (3, TIMESTAMPADD(DAY,1, NOW()), 1, 2);
