package roomescape.service.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ReservationTimeListResponse {
    private List<ReservationTimeResponse> times;

    @JsonCreator
    public ReservationTimeListResponse(List<ReservationTimeResponse> times) {
        this.times = times;
    }

    public List<ReservationTimeResponse> getTimes() {
        return times;
    }
}
