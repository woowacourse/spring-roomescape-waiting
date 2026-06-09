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
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;

@JdbcTest
@ActiveProfiles("test")
@Import({ReservationHistoryService.class, ReservationHistoryDao.class})
public class ReservationHistoryServiceIntegrationTest {

    private static final long RESERVATION_ID = 10L;
    private static final long BROWN_ID = 1L;
    private static final long JEONGKONG_ID = 2L;
    private static final long MANAGER_ID = 99L;

    private final ReservationHistoryService reservationHistoryService;
    private final ReservationHistoryDao reservationHistoryDao;

    @Autowired
    public ReservationHistoryServiceIntegrationTest(ReservationHistoryService reservationHistoryService,
                                                    ReservationHistoryDao reservationHistoryDao) {
        this.reservationHistoryService = reservationHistoryService;
        this.reservationHistoryDao = reservationHistoryDao;
    }

    @Test
    void recordCreated는_CREATED_action으로_history를_저장한다() {
        // given
        Reservation reservation = sampleReservation(BROWN_ID);

        // when
        reservationHistoryService.recordCreated(reservation, BROWN_ID);

        // then
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(RESERVATION_ID);
        assertThat(histories).hasSize(1);
        ReservationHistory history = histories.get(0);
        assertThat(history.getAction()).isEqualTo(ReservationHistoryAction.CREATED);
        assertThat(history.getMemberId()).isEqualTo(BROWN_ID);
        assertThat(history.getActorId()).isEqualTo(BROWN_ID);
    }

    @Test
    void recordUpdated는_UPDATED_action으로_actor와_함께_history를_저장한다() {
        // given
        Reservation reservation = sampleReservation(BROWN_ID);

        // when: 매니저가 사용자 예약을 변경
        reservationHistoryService.recordUpdated(reservation, MANAGER_ID);

        // then
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(RESERVATION_ID);
        assertThat(histories).hasSize(1);
        ReservationHistory history = histories.get(0);
        assertThat(history.getAction()).isEqualTo(ReservationHistoryAction.UPDATED);
        assertThat(history.getMemberId()).isEqualTo(BROWN_ID);
        assertThat(history.getActorId()).isEqualTo(MANAGER_ID);
    }

    @Test
    void recordCanceled는_CANCELED_action으로_history를_저장한다() {
        // given
        Reservation reservation = sampleReservation(BROWN_ID);

        // when
        reservationHistoryService.recordCanceled(reservation, BROWN_ID);

        // then
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(RESERVATION_ID);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getAction()).isEqualTo(ReservationHistoryAction.CANCELED);
    }

    @Test
    void recordTransfer는_TRANSFERRED_OUT과_TRANSFERRED_IN_두_row를_저장한다() {
        // given: BROWN 이 정콩이에게 양도
        Reservation transferredOut = sampleReservation(BROWN_ID);
        Reservation transferredIn = sampleReservation(JEONGKONG_ID);

        // when: actor 는 양도를 트리거한 사람 (BROWN)
        reservationHistoryService.recordTransfer(transferredOut, transferredIn, BROWN_ID);

        // then: 시간순으로 OUT → IN 두 row
        List<ReservationHistory> histories = reservationHistoryDao.findByReservationId(RESERVATION_ID);
        assertThat(histories)
                .hasSize(2)
                .extracting(
                        ReservationHistory::getMemberId,
                        ReservationHistory::getAction,
                        ReservationHistory::getActorId
                )
                .containsExactly(
                        tuple(BROWN_ID, ReservationHistoryAction.TRANSFERRED_OUT, BROWN_ID),
                        tuple(JEONGKONG_ID, ReservationHistoryAction.TRANSFERRED_IN, BROWN_ID)
                );
    }

    private Reservation sampleReservation(Long memberId) {
        return new Reservation(
                RESERVATION_ID,
                memberId,
                LocalDate.of(2026, 12, 1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );
    }
}
