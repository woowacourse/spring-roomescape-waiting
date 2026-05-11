package roomescape.controller.dto;

import java.util.List;

public record ReservationTimeListResponse(
        List<ReservationTimeResponse> times
) {

    public static ReservationTimeListResponse from(List<ReservationTimeResponse> times) {
        return new ReservationTimeListResponse(times);
    }
}
