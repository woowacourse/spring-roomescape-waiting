package roomescape.reservationhistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;

@JdbcTest
@ActiveProfiles("test")
@Import(ReservationHistoryDao.class)
class ReservationHistoryDaoTest {

    private static final String INSERT_THREE_HISTORIES_SQL = """
            INSERT INTO reservation_history
                (id, reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id)
            VALUES
                (1, 1, 1, '2026-12-01', 1, 1, 1, 'CONFIRMED', 1),
                (2, 1, 2, '2026-12-01', 1, 1, 1, 'CANCELED', 1),
                (3, 2, 2, '2026-12-02', 2, 1, 2, 'CONFIRMED', 2);
            """;

    @Autowired
    ReservationHistoryDao reservationHistoryDao;

    @Test
    void 예약_이력을_생성한다() {
        // given: 이력 후보 (id / createdAt null)
        ReservationHistory candidate = new ReservationHistory(
                null,
                1L,
                1L,
                LocalDate.of(2026, 12, 1),
                1L,
                1L,
                1L,
                ReservationHistoryStatus.CONFIRMED,
                1L,
                null
        );

        // when: insert 실행 (DB 가 id 채워서 반환)
        ReservationHistory inserted = reservationHistoryDao.insert(candidate);

        // then: id 가 채워짐 + 다른 필드 보존
        assertThat(inserted.getId()).isPositive();
        assertThat(inserted)
                .extracting(
                        ReservationHistory::getReservationId,
                        ReservationHistory::getMemberId,
                        ReservationHistory::getStatus,
                        ReservationHistory::getActorId
                )
                .containsExactly(1L, 1L, ReservationHistoryStatus.CONFIRMED, 1L);
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void reservationId로_이력_목록을_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(1L);

        // then: reservation(1) 의 이력 2건 (BROWN 양도 후 정콩이 입장 — 하지만 픽스처상 양쪽 다 reservation_id=1)
        assertThat(histories)
                .hasSize(2)
                .extracting(
                        ReservationHistory::getMemberId,
                        ReservationHistory::getStatus
                )
                .containsExactly(
                        tuple(1L, ReservationHistoryStatus.CONFIRMED),
                        tuple(2L, ReservationHistoryStatus.CANCELED)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void memberId로_이력_목록을_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByMemberId(2L);

        // then: member(2) 의 이력 2건 (reservation 1 CANCELED, reservation 2 CONFIRMED)
        assertThat(histories)
                .hasSize(2)
                .extracting(
                        ReservationHistory::getReservationId,
                        ReservationHistory::getStatus
                )
                .containsExactly(
                        tuple(1L, ReservationHistoryStatus.CANCELED),
                        tuple(2L, ReservationHistoryStatus.CONFIRMED)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void storeId로_이력_목록을_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByStoreId(1L);

        // then: store(1) 의 이력 2건
        assertThat(histories)
                .hasSize(2)
                .extracting(
                        ReservationHistory::getStoreId,
                        ReservationHistory::getStatus
                )
                .containsExactly(
                        tuple(1L, ReservationHistoryStatus.CONFIRMED),
                        tuple(1L, ReservationHistoryStatus.CANCELED)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void 원_예약이_없어도_이력은_그대로_남아있다() {
        // given: reservation 테이블에 row 없음 (FK 없으므로 이력만 단독 존재)

        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(1L);

        // then
        assertThat(histories).hasSize(2);
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void 예약_변경_시_CONFIRMED_이력_스냅샷이_동기화된다() {
        // given: 변경된 reservation (reservation_id=1, member_id=1, 새 date/time)
        Reservation updated = new Reservation(
                1L,
                1L,
                LocalDate.of(2027, 1, 15),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                1L,
                1L
        );

        // when: 스냅샷 동기화
        int affected = reservationHistoryDao.updateSnapshot(updated);

        // then: 해당 (reservation_id, member_id) 의 row 가 1건 update
        assertThat(affected).isEqualTo(1);

        // then: 변경된 값 반영
        ReservationHistory history = reservationHistoryDao.findByReservationId(1L).stream()
                .filter(h -> h.getMemberId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertThat(history.getDate()).isEqualTo(LocalDate.of(2027, 1, 15));
        assertThat(history.getTimeId()).isEqualTo(2L);
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void 예약_취소_시_CONFIRMED_이력이_CANCELED로_전환된다() {
        // given: reservation_id=1, member_id=1 의 CONFIRMED 이력

        // when
        int affected = reservationHistoryDao.markCanceled(1L, 1L);

        // then
        assertThat(affected).isEqualTo(1);
        ReservationHistory history = reservationHistoryDao.findByReservationId(1L).stream()
                .filter(h -> h.getMemberId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertThat(history.getStatus()).isEqualTo(ReservationHistoryStatus.CANCELED);
    }
}
