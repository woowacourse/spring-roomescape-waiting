package roomescape.dto;

import java.time.LocalTime;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
}
