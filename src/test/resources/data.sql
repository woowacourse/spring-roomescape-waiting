INSERT INTO reservation_time (start_at)
VALUES ('10:00:00'), -- 삭제 가능
       ('11:00:00'),
       ('12:00:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('이름1', '설명1', '썸네일1'), -- 삭제 가능
       ('이름2', '설명2', '썸네일2'),
       ('이름3', '설명3', '썸네일3');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'admin@email.com', 'admin', 'ADMIN'),
       ('고객1', 'customer1@email.com', 'customer1', 'CUSTOMER'),
       ('고객2', 'customer2@email.com', 'customer2', 'CUSTOMER');

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2024-05-01', 2, 2),
       (2, '2024-05-02', 2, 3),
       (2, '2024-05-01', 3, 2),
       (3, '2024-05-02', 3, 3);
