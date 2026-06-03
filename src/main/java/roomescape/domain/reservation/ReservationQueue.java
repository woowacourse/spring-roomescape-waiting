package roomescape.domain.reservation;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.infra.queue.AsyncQueue;

@Component
public class ReservationQueue extends AsyncQueue<ReservationRequest, ReservationResponse> {

    @Override
    protected String toJobId(ReservationRequest request) {
        return request.date() + ":" + request.timeId() + ":" + request.themeId() + ":" + request.name();
    }
}
