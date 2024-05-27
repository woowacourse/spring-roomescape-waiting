package roomescape.service.booking.waiting;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.service.booking.reservation.module.ReservationCancelService;
import roomescape.service.booking.waiting.module.WaitingCancelService;
import roomescape.service.booking.waiting.module.WaitingRegisterService;
import roomescape.service.booking.waiting.module.WaitingSearchService;

@Service
public class WaitingService {

    private final ReservationCancelService reservationCancelService;
    private final WaitingRegisterService waitingRegisterService;
    private final WaitingSearchService waitingSearchService;
    private final WaitingCancelService waitingCancelService;

    public WaitingService(ReservationCancelService reservationCancelService,
                          WaitingRegisterService waitingRegisterService,
                          WaitingSearchService waitingSearchService,
                          WaitingCancelService waitingCancelService
    ) {
        this.reservationCancelService = reservationCancelService;
        this.waitingRegisterService = waitingRegisterService;
        this.waitingSearchService = waitingSearchService;
        this.waitingCancelService = waitingCancelService;
    }

    public Long resisterWaiting(ReservationRequest request) {
        return waitingRegisterService.registerWaiting(request);
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        return waitingSearchService.findAllWaitingReservations();
    }
    
    public void cancelWaitingForUser(Long reservationId) {
        waitingCancelService.cancelWaitingForUser(reservationId);
    }

    public void cancelWaiting(Long waitingId) {
        waitingCancelService.cancelWaiting(waitingId);
    }
}
