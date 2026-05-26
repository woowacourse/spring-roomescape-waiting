package roomescape.controller.dto.response;

import roomescape.service.dto.AvailableTimes;
import roomescape.service.dto.TimeAvailability;

import java.util.List;

public record ThemeAvailableTimesResponse(
        List<ThemeAvailableTimeResponse> times
) {
    public static ThemeAvailableTimesResponse from(AvailableTimes availableTimes) {
        return new ThemeAvailableTimesResponse(toResponses(availableTimes.values()));
    }

    private static List<ThemeAvailableTimeResponse> toResponses(List<TimeAvailability> timeAvailabilities) {
        return timeAvailabilities.stream()
                .map(ThemeAvailableTimeResponse::from)
                .toList();
    }
}
