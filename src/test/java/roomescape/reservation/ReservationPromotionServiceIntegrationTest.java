package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.application.ReservationPromotionService;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Import(TestTimeConfig.class)
@SpringBootTest
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class ReservationPromotionServiceIntegrationTest {

    @Autowired
    private ReservationPromotionService reservationPromotionService;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("선두 대기 삭제 후 예약 삭제에 실패하면 기존 예약과 선두 대기가 모두 롤백된다.")
    void cancelReservationAndPromoteFirstWaiting_트랜잭션_테스트_1() {
        long reservationId = 1L;
        long scheduleId = 1L;
        Waiting firstWaiting = waitingRepository.save(new Waiting(null, 2L, scheduleId));
        doThrow(new RuntimeException("예약 삭제 실패"))
                .when(reservationRepository)
                .deleteById(reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        assertThatThrownBy(() -> reservationPromotionService.cancelReservationAndPromoteFirstWaiting(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예약 삭제 실패");

        assertThat(reservationRepository.findById(reservationId)).isPresent();
        assertThat(waitingRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.existsByMemberIdAndScheduleId(firstWaiting.getMemberId(), scheduleId))
                .isFalse();
    }

    @Test
    @DisplayName("선두 대기와 기존 예약 삭제 후 승격 예약 저장에 실패하면 기존 예약과 선두 대기가 모두 롤백된다.")
    void cancelReservationAndPromoteFirstWaiting_트랜잭션_테스트_2() {
        long reservationId = 1L;
        long scheduleId = 1L;
        Waiting firstWaiting = waitingRepository.save(new Waiting(null, 2L, scheduleId));
        doThrow(new RuntimeException("승격 예약 저장 실패"))
                .when(reservationRepository)
                .save(any(Reservation.class));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        assertThatThrownBy(() -> reservationPromotionService.cancelReservationAndPromoteFirstWaiting(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("승격 예약 저장 실패");

        assertThat(reservationRepository.findById(reservationId)).isPresent();
        assertThat(waitingRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.existsByMemberIdAndScheduleId(firstWaiting.getMemberId(), scheduleId))
                .isFalse();
    }

    @Test
    @DisplayName("선두 대기 삭제 후 예약 스케줄 변경에 실패하면 기존 예약과 선두 대기가 모두 롤백된다.")
    void changeReservationScheduleAndPromoteFirstWaiting_트랜잭션_테스트_1() {
        long reservationId = 1L;
        long oldScheduleId = 1L;
        long newScheduleId = 4L;
        Waiting firstWaiting = waitingRepository.save(new Waiting(null, 2L, oldScheduleId));
        doThrow(new RuntimeException("예약 스케줄 변경 실패"))
                .when(reservationRepository)
                .updateScheduleById(reservationId, newScheduleId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        assertThatThrownBy(() -> reservationPromotionService.changeReservationScheduleAndPromoteFirstWaiting(
                reservation,
                newScheduleId
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예약 스케줄 변경 실패");

        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow();
        assertThat(foundReservation.getScheduleId()).isEqualTo(oldScheduleId);
        assertThat(waitingRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.existsByMemberIdAndScheduleId(firstWaiting.getMemberId(), oldScheduleId))
                .isFalse();
    }

    @Test
    @DisplayName("선두 대기 삭제와 예약 스케줄 변경 후 승격 예약 저장에 실패하면 기존 예약과 선두 대기가 모두 롤백된다.")
    void changeReservationScheduleAndPromoteFirstWaiting_트랜잭션_테스트_2() {
        long reservationId = 1L;
        long oldScheduleId = 1L;
        long newScheduleId = 4L;
        Waiting firstWaiting = waitingRepository.save(new Waiting(null, 2L, oldScheduleId));
        doThrow(new RuntimeException("승격 예약 저장 실패"))
                .when(reservationRepository)
                .save(any(Reservation.class));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        assertThatThrownBy(() -> reservationPromotionService.changeReservationScheduleAndPromoteFirstWaiting(
                reservation,
                newScheduleId
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("승격 예약 저장 실패");

        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow();
        assertThat(foundReservation.getScheduleId()).isEqualTo(oldScheduleId);
        assertThat(waitingRepository.findById(firstWaiting.getId())).isPresent();
        assertThat(reservationRepository.existsByMemberIdAndScheduleId(firstWaiting.getMemberId(), oldScheduleId))
                .isFalse();
    }
}
