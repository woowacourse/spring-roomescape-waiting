SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO time (start_at)
VALUES ('15:40'),
       ('13:40'),
       ('17:40');

INSERT INTO member (name, email, password, role)
VALUES ('어드민 폴라', 'polla@gmail.com', 'pollari99', 'ADMIN'),
       ('일반 멤버 폴라', 'polla@naver.com', 'pollari999', 'MEMBER'),
       ('일반 멤버 아서', 'dktj@gmail.com', 'pollari9', 'MEMBER'),
       ('일반 멤버 범블비', 'qjaqmfql@gmail.com', 'pollari9', 'MEMBER'),
       ('일반 멤버 폴라포', 'vhffkvh@gmail.com', 'pollari9', 'MEMBER'),
       ('일반 멤버 미르', 'alfm@gmail.com', 'pollari9', 'MEMBER'),
       ('일반 멤버 리브', 'flqm@gmail.com', 'pollari9', 'MEMBER');


INSERT INTO theme (name, description, thumbnail)
VALUES ('그림자 없는 상자', '미안해 누나. 근데 내가 맞았어. 결국 끝도, 시작도, 누나한테 달렸어.',
        'https://cdn.imweb.me/thumbnail/20211118/dc77d37d458b0.jpeg'),
       ('사람들은 그것을 행복이라 부르기로 했다', '석우는 연기를 들이키며 영원이라는 것에 대해 생각해보았다.',
        'https://cdn.imweb.me/thumbnail/20220119/5654d99aeb966.jpg'),
       ('쓰여진 문장 속에 구원이 없다면', '딱 발 한 폭 너비의 벼랑을 생각해보라. 아래를 내려다보면, 아찔한 공허',
        'https://cdn.imweb.me/thumbnail/20240425/22ddac7689249.png'),
       ('존재할 자격', '누구에게 물어도 그는 별 볼 일 없는 사람이었지만, 그 자신은 제법 만족스럽게 살았다.',
        'https://cdn.imweb.me/thumbnail/20240425/c79ae78193e55.png'),
       ('뱃사람의 별', '나는 내 한 몸 들어가는 쪽배를 타고 밤바다를 부유하곤 합니다.', 'https://cdn.imweb.me/thumbnail/20240425/d8a954954a4fb.jpeg');

INSERT INTO reservation (date, time_id, theme_id, member_id, reservation_status)
VALUES (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 1, 'RESERVED'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 2, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 3, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 4, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 5, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 6, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 3, 2, 'RESERVED'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 4, 3, 'RESERVED'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 3, 3, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 5, 2, 'RESERVED'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 5, 3, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 2, 2, 'RESERVED'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 3, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 1, 3, 3, 'WAITING'),
       (CURRENT_DATE + INTERVAL '1' DAY , 2, 2, 2, 'RESERVED');
