package roomescape.domain.reservation;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.infra.queue.AsyncQueue;
import roomescape.infra.queue.JobResult;

@Component
public class ReservationQueue extends AsyncQueue<ReservationRequest, ReservationResponse> {

    private final ReservationService reservationService;

    public ReservationQueue(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Override
    protected String toSlotId(ReservationRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId();
    }

    @Override
    protected String toJobId(ReservationRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId() + ":" + request.name();
    }

    @Override
    protected JobResult<ReservationResponse> process(ReservationRequest request) {
        try {
            return JobResult.success(reservationService.createReservation(request));
        } catch (RoomescapeException e) {
            return JobResult.failed(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void evictExpiredResults() {
        evictBefore(LocalDate.now());
    }
}