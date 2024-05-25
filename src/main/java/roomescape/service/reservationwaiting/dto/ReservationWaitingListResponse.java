package roomescape.service.reservationwaiting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ReservationWaitingListResponse {
    private List<ReservationWaitingResponse> waitings;

    @JsonCreator
    public ReservationWaitingListResponse(List<ReservationWaitingResponse> waitings) {
        this.waitings = waitings;
    }

    public List<ReservationWaitingResponse> getWaitings() {
        return waitings;
    }
}
