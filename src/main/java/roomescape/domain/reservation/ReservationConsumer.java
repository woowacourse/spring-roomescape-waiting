package roomescape.domain.reservation;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.infra.queue.AsyncConsumer;
import roomescape.infra.queue.JobResult;

@Component
public class ReservationConsumer extends AsyncConsumer<ReservationRequest, ReservationResponse> {

    private final ReservationService reservationService;

    public ReservationConsumer(ReservationQueue reservationQueue, ReservationService reservationService) {
        super(reservationQueue);
        this.reservationService = reservationService;
    }

    @Override
    protected String threadName() {
        return "reservation-consumer";
    }

    @Override
    protected JobResult<ReservationResponse> process(ReservationRequest request) {
        try {
            ReservationResponse response = reservationService.createReservation(request);
            return JobResult.success(response);
        } catch (RoomescapeException e) {
            return JobResult.failed(e.getMessage());
        }
    }
}