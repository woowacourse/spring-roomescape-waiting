package roomescape.reservationhistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@JdbcTest
@ActiveProfiles("test")
@Import(ReservationHistoryDao.class)
class ReservationHistoryDaoTest {

    private static final String INSERT_THREE_HISTORIES_SQL = """
            INSERT INTO reservation_history
                (id, reservation_id, member_id, date, time_id, theme_id, store_id, action, actor_id, created_at)
            VALUES
                (1, 1, 1, '2026-12-01', 1, 1, 1, 'CREATED',         1, '2026-06-01 10:00:00'),
                (2, 1, 1, '2026-12-01', 1, 1, 1, 'TRANSFERRED_OUT', 1, '2026-06-02 10:00:00'),
                (3, 1, 2, '2026-12-01', 1, 1, 1, 'TRANSFERRED_IN',  1, '2026-06-02 10:00:00'),
                (4, 2, 2, '2026-12-02', 2, 1, 2, 'CREATED',         2, '2026-06-03 10:00:00');
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
                ReservationHistoryAction.CREATED,
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
                        ReservationHistory::getAction,
                        ReservationHistory::getActorId
                )
                .containsExactly(1L, 1L, ReservationHistoryAction.CREATED, 1L);
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void reservationId로_이력_목록을_시간순_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(1L);

        // then: reservation(1) 의 이력 3건이 created_at ASC, id ASC 순으로
        assertThat(histories)
                .extracting(
                        ReservationHistory::getMemberId,
                        ReservationHistory::getAction
                )
                .containsExactly(
                        tuple(1L, ReservationHistoryAction.CREATED),
                        tuple(1L, ReservationHistoryAction.TRANSFERRED_OUT),
                        tuple(2L, ReservationHistoryAction.TRANSFERRED_IN)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void memberId로_이력_목록을_최신순_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByMemberId(2L);

        // then: member(2) 의 이력 2건이 created_at DESC, id DESC 순으로
        assertThat(histories)
                .extracting(
                        ReservationHistory::getReservationId,
                        ReservationHistory::getAction
                )
                .containsExactly(
                        tuple(2L, ReservationHistoryAction.CREATED),
                        tuple(1L, ReservationHistoryAction.TRANSFERRED_IN)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void storeId로_이력_목록을_최신순_조회한다() {
        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByStoreId(1L);

        // then: store(1) 의 이력 3건이 created_at DESC, id DESC 순으로
        assertThat(histories)
                .extracting(
                        ReservationHistory::getStoreId,
                        ReservationHistory::getAction
                )
                .containsExactly(
                        tuple(1L, ReservationHistoryAction.TRANSFERRED_IN),
                        tuple(1L, ReservationHistoryAction.TRANSFERRED_OUT),
                        tuple(1L, ReservationHistoryAction.CREATED)
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_HISTORIES_SQL)
    void 원_예약이_없어도_이력은_그대로_남아있다() {
        // given: reservation 테이블에 row 없음 (FK 없으므로 이력만 단독 존재)

        // when
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(1L);

        // then
        assertThat(histories).hasSize(3);
    }

}
