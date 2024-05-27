package roomescape.service.booking.waiting;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.service.booking.reservation.module.ReservationCancelService;
import roomescape.service.booking.waiting.module.WaitingCancelService;
import roomescape.service.booking.waiting.module.WaitingResisterService;
import roomescape.service.booking.waiting.module.WaitingSearchService;

@Service
public class WaitingService {

    private final ReservationCancelService reservationCancelService;
    private final WaitingResisterService waitingResisterService;
    private final WaitingSearchService waitingSearchService;
    private final WaitingCancelService waitingCancelService;

    public WaitingService(ReservationCancelService reservationCancelService,
                          WaitingResisterService waitingResisterService,
                          WaitingSearchService waitingSearchService,
                          WaitingCancelService waitingCancelService
    ) {
        this.reservationCancelService = reservationCancelService;
        this.waitingResisterService = waitingResisterService;
        this.waitingSearchService = waitingSearchService;
        this.waitingCancelService = waitingCancelService;
    }

    public Long resisterWaiting(ReservationRequest request) {
        return waitingResisterService.registerWaiting(request);
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        return waitingSearchService.findAllWaitingReservations();
    }
    
    public void cancelWaitingForUser(Long reservationId) {
        waitingCancelService.cancelWaitingForUser(reservationId);
    }

    public void cancelWaiting(Long waitingId) {
        waitingCancelService.deleteWaiting(waitingId);
    }
}
