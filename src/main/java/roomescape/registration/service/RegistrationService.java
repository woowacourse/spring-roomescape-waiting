package roomescape.registration.service;

import org.springframework.stereotype.Service;
import roomescape.registration.domain.reservation.dto.ReservationRequest;
import roomescape.registration.domain.reservation.service.ReservationService;
import roomescape.registration.domain.waiting.domain.Waiting;
import roomescape.registration.domain.waiting.service.WaitingService;

@Service
public class RegistrationService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public RegistrationService(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    // todo: 예약대기 승인이 가능한지 validate 한다.
    public void approveWaitingToReservation(long waitingId) {
        Waiting waiting = waitingService.findWaitingById(waitingId);

        reservationService.addReservation(new ReservationRequest(
                        waiting.getDate(),
                        waiting.getReservationTime().getId(),
                        waiting.getTheme().getId()
                ), waiting.getMember().getId()
        );

        waitingService.removeWaiting(waitingId);
    }
}
