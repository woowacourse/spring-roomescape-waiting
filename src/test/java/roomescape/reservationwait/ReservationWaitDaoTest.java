package roomescape.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationwait.dto.WaitingProjection;

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

    private static final String INSERT_TWO_RESERVATION_WAITS_SQL = """
            INSERT INTO reservation_wait (id, reservation_id, member_id)
            VALUES (1, 1, 2),
                   (2, 2, 1);
            """;

    private static final String INSERT_MULTIPLE_WAITS_SQL = """
          INSERT INTO reservation_wait (id, reservation_id, member_id)
          VALUES (1, 1, 1),
                 (2, 1, 2),
                 (3, 2, 1);
          """;

    @Autowired
    ReservationWaitDao reservationWaitDao;

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
    })
    void 예약대기를_생성한다() {
        // given: 대기 후보 (id / createdAt null)
        long reservationId = 1L;
        long memberId = 2L;
        ReservationWait candidate = new ReservationWait(null, reservationId, memberId, null);

        // when: insert 실행 (DB 가 id + createdAt 채워서 반환)
        ReservationWait inserted = reservationWaitDao.insert(candidate);

        // then: DB 가 채운 필드 검증
        assertThat(inserted.getId()).isPositive();
        assertThat(inserted)
                .extracting(
                        ReservationWait::getReservationId,
                        ReservationWait::getMemberId
                )
                .containsExactly(reservationId, memberId);
        assertThat(inserted.getCreatedAt()).isNotNull();
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

        Optional<ReservationWait> wait = reservationWaitDao.findReservationWaitById(waitId);
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

        Optional<ReservationWait> wait = reservationWaitDao.findReservationWaitById(waitId);
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
        // given: wait(id=1) 의 reservation_id=1, member_id=2
        long reservationId = 1L;
        long memberId = 2L;

        // when: 복합 키로 삭제
        reservationWaitDao.deleteByReservationIdAndMemberId(reservationId, memberId);

        // then: 해당 wait row 사라짐
        Optional<ReservationWait> wait = reservationWaitDao.findReservationWaitById(1L);
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
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberId(1L);

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
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberId(1L);
        assertThat(earliestMemberId).isEmpty();
    }

    @Test
    @Sql("/member-waitings.sql")
    void memberId로_내_예약대기_목록을_조회한다() {
        List<WaitingProjection> waitingResponseProjections = reservationWaitDao.findWaitingsByMemberId(1L);

        assertThat(waitingResponseProjections)
                .hasSize(3)
                .extracting(
                        WaitingProjection::reservationId,
                        WaitingProjection::memberId
                )
                .containsExactly(
                        tuple(1L, 1L),
                        tuple(2L, 1L),
                        tuple(3L, 1L)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
            INSERT_MULTIPLE_WAITS_SQL
    })
    void reservationId에_해당하는_모든_대기를_삭제한다() {
        // given: reservation(1) 에 wait 2개, reservation(2) 에 wait 1개

        // when
        reservationWaitDao.deleteAllByReservationId(1L);

        // then
        assertThat(reservationWaitDao.findReservationWaitById(1L)).isEmpty();
        assertThat(reservationWaitDao.findReservationWaitById(2L)).isEmpty();

        assertThat(reservationWaitDao.findReservationWaitById(3L)).isPresent();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL,
            """
            INSERT INTO reservation_wait (id, reservation_id, member_id, created_at)
            VALUES (1, 1, 2, '2026-06-05 10:00:00'),
                   (2, 1, 1, '2026-06-05 10:00:00');
            """
    })
    void created_at이_같으면_id가_작은_대기자를_먼저_반환한다() {
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberId(1L);

        assertThat(earliestMemberId).isPresent();
        assertThat(earliestMemberId.get()).isEqualTo(2L);
    }
}
