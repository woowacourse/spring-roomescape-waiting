INSERT INTO member(name, email, password, role) VALUES ('켬미', 'aaa@naver.com', '1111', 'USER');
INSERT INTO member(name, email, password, role) VALUES ('관리자', 'admin@naver.com', '1111', 'ADMIN');

INSERT INTO time(start_at) VALUES ('09:00');
INSERT INTO time(start_at) VALUES ('10:00');
INSERT INTO time(start_at) VALUES ('23:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('제로 공포', '마지막까지 저희와 함께 해 주시겠습니까?',
        'https://mblogthumb-phinf.pstatic.net/MjAyMjEyMTBfMjAz/MDAxNjcwNjYwMjk5NTE3.srbGxur7u_MB95L5xEozJBHXu-yNu0euNFGVRMhWfs8g.-IDeYNVq6n7XvW4MItolamOrSkhGPAiMnikLB0DrzXEg.PNG.pkloijku1/image.png?type=w800');
INSERT INTO theme(name, description, thumbnail)
VALUES ('슬래셔', '박철민의 뇌세포를 찾은 이혁. 의문점을 가지고 뇌세포를 조작한 곳을 찾아가게 되는데...',
        'https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyMzExMTZfMTQx%2FMDAxNzAwMTQxNDIzOTM0.dNWissWfHWlesq4L6KLET58Kci_N-vR83qNFgtEeBoQg.XmdvOatL3XXydJzNr3K5fALsfEUasq8lTGd7O6CCUW4g.JPEG.hyemi_406%2FIMG_4551.jpg&type=sc960_832');
INSERT INTO theme(name, description, thumbnail)
VALUES ('짱구를 찾아서', '짱구는 어디에 있을까?',
        'https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDAzMDZfMTI1%2FMDAxNzA5NzE0NzIzNjAx.QIkFPA7LK4SoNR9-SyckukAsxIcR4tSZQWqq1Emyb8sg.oq2m0SOEsCVuzTlbhjV374cB275bJp3My8479YGrph4g.JPEG%2FIMG_7434.jpg&type=a340');
INSERT INTO theme(name, description, thumbnail)
VALUES ('우테코 살아남기', '우테코는 어떤 곳인가 과연.. 그곳에 찾아간...',
        'https://techblog.woowahan.com/wp-content/uploads/img/2019-02-08/techcourse_poster.jpeg');

INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 5, 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 4, 1, 2, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 5, 1, 2, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 3, 1, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 3, 1, 2, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 2, 1, 1, 3);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 2, 1, 2, 3);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (CURRENT_DATE - 1, 1, 1, 1);
