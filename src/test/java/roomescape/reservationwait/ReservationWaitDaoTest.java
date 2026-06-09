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
            INSERT_TWO_RESERVATIONS_SQL
    })
    void id로_예약대기를_조회한다() {
        // given
        ReservationWait jeongkongWait = reservationWaitDao.insert(
                new ReservationWait(null, 1L, 2L, null));

        // when
        Optional<ReservationWait> wait = reservationWaitDao.findReservationWaitById(jeongkongWait.getId());

        // then
        assertTrue(wait.isPresent());
        assertThat(wait.get())
                .extracting(
                        ReservationWait::getReservationId,
                        ReservationWait::getMemberId
                )
                .containsExactly(1L, 2L);
    }

    @Test
    void 존재하지_않는_id면_빈_Optional을_반환한다() {
        // when
        Optional<ReservationWait> wait = reservationWaitDao.findReservationWaitById(999L);

        // then
        assertThat(wait.isEmpty()).isTrue();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void reservationId와_memberId로_예약대기를_삭제한다() {
        // given
        ReservationWait jeongkongWait = reservationWaitDao.insert(
                new ReservationWait(null, 1L, 2L, null));

        // when
        reservationWaitDao.deleteByReservationIdAndMemberId(1L, 2L);

        // then
        assertThat(reservationWaitDao.findReservationWaitById(jeongkongWait.getId())).isEmpty();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 가장_먼저_예약대기한_memberId를_반환한다() {
        // given
        reservationWaitDao.insert(new ReservationWait(null, 1L, 1L, null));
        reservationWaitDao.insert(new ReservationWait(null, 1L, 2L, null));

        // when
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberIdForUpdate(1L);

        // then
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
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberIdForUpdate(1L);
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
            INSERT_TWO_RESERVATIONS_SQL
    })
    void reservationId에_해당하는_모든_대기를_삭제한다() {
        // given
        ReservationWait waitOnReservation1ByBrown = reservationWaitDao.insert(
                new ReservationWait(null, 1L, 1L, null));
        ReservationWait waitOnReservation1ByJeongkong = reservationWaitDao.insert(
                new ReservationWait(null, 1L, 2L, null));
        ReservationWait waitOnReservation2ByBrown = reservationWaitDao.insert(
                new ReservationWait(null, 2L, 1L, null));

        // when
        reservationWaitDao.deleteAllByReservationId(1L);

        // then
        assertThat(reservationWaitDao.findReservationWaitById(waitOnReservation1ByBrown.getId())).isEmpty();
        assertThat(reservationWaitDao.findReservationWaitById(waitOnReservation1ByJeongkong.getId())).isEmpty();
        assertThat(reservationWaitDao.findReservationWaitById(waitOnReservation2ByBrown.getId())).isPresent();
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
        Optional<Long> earliestMemberId = reservationWaitDao.findEarliestMemberIdForUpdate(1L);

        assertThat(earliestMemberId).isPresent();
        assertThat(earliestMemberId.get()).isEqualTo(2L);
    }
}
