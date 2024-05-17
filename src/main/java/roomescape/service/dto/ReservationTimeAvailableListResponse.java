package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ReservationTimeAvailableListResponse {
    private final List<ReservationTimeAvailableResponse> times;
    
    @JsonCreator
    public ReservationTimeAvailableListResponse(List<ReservationTimeAvailableResponse> times) {
        this.times = times;
    }

    public List<ReservationTimeAvailableResponse> getTimes() {
        return times;
    }
}
