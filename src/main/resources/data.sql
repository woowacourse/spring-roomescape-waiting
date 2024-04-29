INSERT INTO reservation_time (start_at)
VALUES ('10:00:00'),
       ('11:00:00'),
       ('12:00:00'),
       ('13:00:00'),
       ('14:00:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('우테코 수료 대장정',
        '수료를 못 할 위기에 놓인 크루를 구하라!',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQxwhvlMeM3WyfMxfxvP0QnhK0rTyvWK7cLlkRfMV_EJO3HYsvtdM3FZdXqWN7-T5G0eKg&usqp=CAU'),
       ('현실에선 백수인 내가 이세계에서는 반란군?!',
        '포비의 가르침에 따라 반란군에 합류하고자 하는 크루들은 무사히 취업에 성공할 수 있을까?',
        'https://likenovel.kr.object.ncloudstorage.com/titles/thumbnails/eSw6z8swjUiQLb21KbnrjN9eKKrcNhoAAi1mXhvt.png'),
       ('자바 실종 사건',
        '자바가 사라진 세상에서 어셈블리어를 사용해야 하는 크루원들을 도와 웹 어플리케이션을 완성해 보자!',
        'https://blog.kakaocdn.net/dn/bhUJiY/btrPwZSMBHk/4FckV4mu6jKKcqs3SXAP30/img.png'),
       ('나 아직 있는데..',
        '자고 있는 사이에 캠퍼스 문이 닫혀버렸다! 잠실 캠퍼스 탈출기',
        'https://velog.velcdn.com/images/jangws/post/7199f6ba-b3a8-4e2f-b86a-e5406b166bcd/image.jpeg');

INSERT INTO member (name, email, password, role)
VALUES ('산초', 'sancho@sancho.com', 'sancho', 'ADMIN'),
       ('찰리', 'charlie@sancho.com', 'charlie', 'CUSTOMER'),
       ('알파카', 'alpaca@sancho.com', 'alpaca', 'CUSTOMER'),
       ('위브', 'wiib@sancho.com', 'wiib', 'CUSTOMER');

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2024-05-10', 1, 1),
       (3, '2024-05-11', 3, 2),
       (3, '2024-05-12', 4, 3),
       (4, '2024-05-13', 4, 2),
       (4, '2024-05-14', 3, 4),
       (4, '2024-05-15', 2, 3);
