package roomescape.reservationtime.controller.dto;

import java.util.List;

public record AvailableTimeListResponse(
        List<AvailableTimeResponse> availableTimes
) {

    public static AvailableTimeListResponse from(List<AvailableTimeResponse> availableTimes) {
        return new AvailableTimeListResponse(availableTimes);
    }
}
