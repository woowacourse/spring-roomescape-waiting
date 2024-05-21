package roomescape.domain.dto;

import java.util.List;

public class ReservationTimeResponses {
    private final List<ReservationTimeResponse> data;

    public ReservationTimeResponses(final List<ReservationTimeResponse> data) {
        this.data = data;
    }

    public List<ReservationTimeResponse> getData() {
        return data;
    }
}
