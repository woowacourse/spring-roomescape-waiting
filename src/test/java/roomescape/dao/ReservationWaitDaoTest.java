package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationWait;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(ReservationWaitDao.class)
@ActiveProfiles("test")
@JdbcTest
class ReservationWaitDaoTest {

    private static final String INSERT_THREE_TIMES_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00'),
                   (2, '11:00'),
                   (3, '13:00');
            """;

    private static final String INSERT_SINGLE_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg');
            """;

    private static final String INSERT_TWO_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name)
            VALUES (1, 'brown@email.com', 'password', '브라운'),
                   (2, 'jeongkong@email.com', 'password', '정콩이');
            """;

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_TWO_RESERVATIONS_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 1, 1),
                   (2, 2, '2026-05-02', 2, 1, 1);
            """;

    private static final String INSERT_TWO_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점');
            """;

    private static final String INSERT_RESERVATIONS_ACROSS_STORES_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 1, 1),
                   (2, 2, '2026-05-02', 2, 1, 1),
                   (3, 1, '2026-05-03', 3, 1, 2);
            """;

    private static final String INSERT_TWO_RESERVATION_WAITS_SQL = """
            INSERT INTO reservation_wait (id, reservation_id, member_id)
            VALUES (1, 1, 2),
                   (2, 2, 1);
            """;

    @Autowired
    ReservationWaitDao dao;

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
    })
    void 예약_대기_생성() {
        long reservationId = 1L;
        long memberId = 2L;

        long waitId = dao.createReservationWait(memberId, reservationId);

        assertThat(waitId).isPositive();
        assertThat(waitId).isEqualTo(1);

        ReservationWait wait = dao.findReservationWaitById(waitId).get();
        assertThat(wait)
                .extracting(
                        ReservationWait::getId,
                        ReservationWait::getReservationId,
                        ReservationWait::getMemberId
                )
                .containsExactly(waitId, reservationId, memberId);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
            INSERT_TWO_RESERVATION_WAITS_SQL
    })
    void 예약대기_단건_조회성공() {
        long waitId = 1L;

        Optional<ReservationWait> wait = dao.findReservationWaitById(waitId);
        assertTrue(wait.isPresent());
        assertThat(wait.get())
                .extracting(
                        ReservationWait::getId,
                        ReservationWait::getReservationId,
                        ReservationWait::getMemberId
                )
                .containsExactly(waitId, 1L, 2L);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
            INSERT_TWO_RESERVATION_WAITS_SQL
    })
    void 예약대기_단건_조회실패() {
        long waitId = 3L;

        Optional<ReservationWait> wait = dao.findReservationWaitById(waitId);
        assertThat(wait.isEmpty()).isTrue();
    }
}