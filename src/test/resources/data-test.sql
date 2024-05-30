insert into reservation_time(start_at)
values ('10:00'),
       ('23:00'),
       ('14:00'),
       ('20:00');
insert into theme(theme_name, description, thumbnail)
values ('링', '이거 겁나 무서움', '링 썸네일'),
       ('도시괴담', '이건 조금 덜 무서움', '도시괴담 썸네일'),
       ('콜러', '공포 테마 중독자 추천', '콜러 썸네일'),
       ('제로', '심약자는 도전하지 마시오', '제로 썸네일');
insert into member(member_name, email, password, member_role)
values ('제리', 'jerry@gmail.com', 'password', 'ADMIN'),
       ('오리', 'duck@gmail.com', 'password', 'MEMBER'),
       ('안나', 'anna@gamil.com', 'password', 'MEMBER');
insert into reservation(date, time_id, theme_id, member_id, status, created_at)
values ('2024-12-12', 1, 1, 1, 'RESERVATION', '2024-12-01 00:00:00.000'),
       ('2024-12-23', 2, 3, 1, 'RESERVATION', '2024-12-02 00:00:00.000'),
       ('2024-12-25', 3, 2, 2, 'RESERVATION', '2024-12-03 00:00:00.000'),
       ('2024-06-30', 1, 2, 1, 'RESERVATION', '2024-12-04 00:00:00.000'),
       ('2024-06-30', 1, 2, 2, 'WAITING', '2024-12-05 00:00:00.000'),
       ('2024-06-30', 1, 2, 3, 'WAITING', '2024-12-06 00:00:00.000');
