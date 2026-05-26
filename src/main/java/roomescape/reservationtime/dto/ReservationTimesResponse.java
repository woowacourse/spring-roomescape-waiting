package roomescape.reservationtime.dto;

import java.util.List;

public record ReservationTimesResponse(
        List<ReservationTimeResponse> times
) {

    public static ReservationTimesResponse from(List<ReservationTimeResponse> times) {
        return new ReservationTimesResponse(times);
    }
}
