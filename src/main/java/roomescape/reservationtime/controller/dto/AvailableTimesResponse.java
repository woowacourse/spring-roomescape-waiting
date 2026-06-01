package roomescape.reservationtime.controller.dto;

import java.util.List;

public record AvailableTimesResponse(
        List<AvailableTimeResponse> availableTimes
) {

    public static AvailableTimesResponse from(List<AvailableTimeResponse> availableTimes) {
        return new AvailableTimesResponse(availableTimes);
    }
}
