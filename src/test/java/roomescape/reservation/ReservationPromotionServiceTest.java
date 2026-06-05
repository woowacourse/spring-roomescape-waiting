package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.ReservationPromotionService;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationPromotionServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @InjectMocks
    private ReservationPromotionService reservationPromotionService;

    @Test
    @DisplayName("선두 대기자가 없으면 예약만 삭제한다.")
    void cancelReservationAndPromoteFirstWaiting_테스트_1() {
        long reservationId = 1L;
        long scheduleId = 10L;
        when(waitingRepository.findFirstByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.empty());

        reservationPromotionService.cancelReservationAndPromoteFirstWaiting(reservationId, scheduleId);

        verify(reservationRepository).deleteById(reservationId);
        verify(waitingRepository, never()).deleteById(anyLong());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("선두 대기자가 있으면 대기를 삭제하고 예약으로 승격한다.")
    void cancelReservationAndPromoteFirstWaiting_테스트_2() {
        long reservationId = 1L;
        long scheduleId = 10L;
        Waiting firstWaiting = new Waiting(100L, 3L, scheduleId);
        when(waitingRepository.findFirstByScheduleIdForPromotion(scheduleId))
                .thenReturn(Optional.of(firstWaiting));

        reservationPromotionService.cancelReservationAndPromoteFirstWaiting(reservationId, scheduleId);

        verify(waitingRepository).deleteById(firstWaiting.getId());
        verify(reservationRepository).deleteById(reservationId);
        verify(reservationRepository).save(argThat(promoted ->
                promoted.getMemberId().equals(firstWaiting.getMemberId())
                        && promoted.getScheduleId().equals(scheduleId)
        ));
    }
}
