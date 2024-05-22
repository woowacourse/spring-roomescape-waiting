package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.BadArgumentRequestException;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@Component
@Transactional
public class ReservationDeleteUsecase {
    private final ReservationFindService reservationFindService;
    private final ReservationUpdateService reservationUpdateService;
    private final WaitingService waitingService;

    public ReservationDeleteUsecase(ReservationFindService reservationFindService,
                                    ReservationUpdateService reservationUpdateService,
                                    WaitingService waitingService) {
        this.reservationFindService = reservationFindService;
        this.reservationUpdateService = reservationUpdateService;
        this.waitingService = waitingService;
    }

    public void deleteReservation(Long reservationId) {
        ReservationResponse reservation = reservationFindService.findReservation(reservationId);
        validateIsAfterFromNow(reservation);

        Optional<WaitingResponse> highPriorityWaiting = waitingService.findHighPriorityWaiting(reservationId);
        if (highPriorityWaiting.isEmpty()) {
            reservationUpdateService.deleteReservation(reservationId);
            return;
        }
        confirmNextWaiting(highPriorityWaiting.get());
    }

    private void validateIsAfterFromNow(ReservationResponse reservation) {
        if (reservation.date().isBefore(LocalDate.now())) {
            throw new BadArgumentRequestException("예약은 현재 날짜 이후여야 합니다.");
        }
    }

    private void confirmNextWaiting(WaitingResponse waiting) {
        waitingService.confirmReservation(waiting.id());
        waitingService.deleteWaiting(waiting.id());
    }
}
