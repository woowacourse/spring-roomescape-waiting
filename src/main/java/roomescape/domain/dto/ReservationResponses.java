package roomescape.domain.dto;

import java.util.List;

public class ReservationResponses {
    private final List<ReservationResponse> data;

    public ReservationResponses(final List<ReservationResponse> data) {
        this.data = data;
    }

    public List<ReservationResponse> getData() {
        return data;
    }
}
