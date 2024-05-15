insert into member (id, email, name, password, role)
values (1, 'mrmrmr@woowa.net', '미르', 'anything', 'ADMIN'),
       (2, 'mangcho@woowa.net', '망쵸', 'nothing', 'NORMAL'),
       (3, 'fram@woowa.net', '프람', 'something', 'NORMAL'),
       (4, 'pobi@woowa.net', '포비', 'everything', 'ADMIN');

insert into theme (name, description, thumbnail)
values ('세렌디피티: 뜻밖의 행운',
        '방탈출 게임은 주어진 시간 내에 팀이 퍼즐을 해결하고 탈출하는 것이 목표입니다. 퍼즐은 다양한 형태로 주어질 수 있으며, 팀원들은 상호 협력하여 각종 단서를 찾고 연결하여 문제를 해결해야 합니다. 성공적으로 퍼즐을 풀고 모든 단서를 이용해 탈출하면 게임을 클리어할 수 있습니다.',
        'https://i.postimg.cc/T2Df9mR3/theme-PNG-SERENDIPITY.png'),
       ('씨프',
        '방탈출 게임에서는 종종 플레이어가 퍼즐을 풀다가 막히는 경우가 있습니다. 이럴 때 플레이어는 게임 진행자 또는 스탭에게 도움을 요청할 수 있습니다. 그러나 게임이 너무 쉬워지거나 플레이어들이 더 이상 과제에 도전하지 않게 되는 것을 막기 위해, 이러한 힌트나 도움은 일정한 규칙에 따라 제공됩니다. 바로 이 규칙에 따라 플레이어가 힌트를 얻을 수 있는 장소를 "씨프" 또는 "C.P."라고 부릅니다.',
        'https://i.postimg.cc/DyP5kj2p/theme-XX.png'),
       ('SOS',
        '플레이어들이 주어진 시간 안에 퍼즐을 해결하고, 숨겨진 단서를 찾아서 탈출하는 것을 목표로 합니다. 대부분의 경우, 게임은 제한된 공간 안에 설정되어 있으며, 플레이어들은 이 공간에서 다양한 퍼즐을 풀어서 출구를 찾아야 합니다.',
        'https://i.postimg.cc/cLqW2JLB/theme-SOS-SOS.jpg'),
       ('데이트 코스 연구회',
        '게임의 시작부터 끝까지, 플레이어들은 로맨틱한 분위기를 느끼며 함께 다양한 퍼즐을 풀고 탈출하기 위해 협력해야 합니다. 일반적으로 "데이트 코스 연구회"의 방탈출 게임은 두 사람 이상의 플레이어가 함께 참여하며, 서로 협력하여 문제를 해결해야 합니다.',
        'https://i.postimg.cc/vDFKqct1/theme.jpg');

insert into reservation_time (start_at)
values ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00'),
       ('19:00'),
       ('20:00'),
       ('21:00');

insert into reservation (member_id, date, time_id, theme_id)
values (1, '2024-05-07', 1, 1),
       (2, '2024-05-08', 2, 1),
       (3, '2024-05-08', 4, 2),
       (4, '2024-05-09', 1, 1);
