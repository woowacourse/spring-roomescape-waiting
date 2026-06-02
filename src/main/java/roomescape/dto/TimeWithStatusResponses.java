package roomescape.dto;

import java.util.List;

public record TimeWithStatusResponses(
        List<TimeWithStatusResponse> times
) {
    public static TimeWithStatusResponses of(List<TimeWithStatusResponse> times) {
        return new TimeWithStatusResponses(times);
    }
}
