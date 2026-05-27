package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationWait;
import roomescape.dto.WaitingResponseProjection;

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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
    })
    void 예약대기를_생성한다() {
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
    void id로_예약대기를_조회한다() {
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
    void 존재하지_않는_id면_빈_Optional을_반환한다() {
        long waitId = 3L;

        Optional<ReservationWait> wait = dao.findReservationWaitById(waitId);
        assertThat(wait.isEmpty()).isTrue();
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
    void reservationId와_memberId로_예약대기를_삭제한다() {
        long reservationId = 1L;
        long memberId = 2L;

        dao.deleteByReservationIdAndMemberId(reservationId, memberId);
        Optional<ReservationWait> wait = dao.findReservationWaitById(1L);
        assertThat(wait.isEmpty()).isTrue();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
            """
                    INSERT INTO reservation_wait (id, reservation_id, member_id)
                    VALUES (1, 1, 1),
                           (2, 1, 2);
                    """
    })
    void 가장_먼저_예약대기한_memberId를_반환한다() {
        Optional<Long> earliestMemberId = dao.findEarliestMemberId(1L);

        assertThat(earliestMemberId).isPresent();
        assertThat(earliestMemberId.get()).isEqualTo(1L);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 예약_대기자가_없으면_먼저_예약대기한_멤버는_없다() {
        Optional<Long> earliestMemberId = dao.findEarliestMemberId(1L);
        assertThat(earliestMemberId).isEmpty();
    }

    @Test
    @Sql("/member-waitings.sql")
    void memberId로_내_예약대기_목록을_조회한다() {
        List<WaitingResponseProjection> waitingResponseProjections = dao.findWaitingsByMemberId(1L);

        assertThat(waitingResponseProjections)
                .hasSize(3)
                .extracting(
                        WaitingResponseProjection::reservationId,
                        WaitingResponseProjection::memberId
                )
                .containsExactly(
                        tuple(1L, 1L),
                        tuple(2L, 1L),
                        tuple(3L, 1L)
                );
    }
}
