MERGE INTO theme(name, description, thumbnail) KEY (name, description, thumbnail) VALUES
    ('시간조작자 연구소', '반복되는 1시간, 탈출 못하면 다시 시작된다.', '/images/themes/1.png'),
    ('사라진 개발자', '흔적만 남긴 채 사라진 개발자의 마지막 로그를 추적한다.', '/images/themes/2.png'),
    ('404호의 비밀', '존재하지 않는 방, 들어간 사람은 돌아오지 않는다.', '/images/themes/3.png'),
    ('녹화된 마지막 하루', '누군가의 마지막 하루를 되돌려 탈출해야 한다.', '/images/themes/4.png'),
    ('VIP 전용 금고', '제한 시간 내 금고를 열지 못하면 시스템이 초기화된다.', '/images/themes/5.png'),
    ('버그 추적자: 죽음의 디버깅', '무한 루프에 빠진 시스템 속에서 치명적인 오류를 찾아라.', '/images/themes/6.png'),
    ('9회말 2사 만루', '승리를 눈앞에 두고 사라진 투수, 마운드 위의 미스터리.', '/images/themes/7.png'),
    ('새벽 2시의 증류소', '금지된 레시피를 훔치러 간 바(Bar), 의문의 알람이 울린다.', '/images/themes/8.png'),
    ('잊혀진 기억의 숲', '잃어버린 과거의 파편을 모아야만 숲의 문이 열린다.', '/images/themes/10.png'),
    ('명탐정의 마지막 조각', '미해결 사건의 결정적 증거가 숨겨진 서재를 탐색하라.', '/images/themes/11.png'),
    ('인공지능의 반란', '통제 불능이 된 메인 프레임을 셧다운하고 기지를 탈출하라.', '/images/themes/12.png'),
    ('한밤중의 도서관', '자정의 종이 울리면 책 속에 갇힌 유령들이 깨어난다.', '/images/themes/13.png');

ALTER TABLE theme ALTER COLUMN id RESTART WITH 13;
