package roomescape.domain.dto;

import java.time.LocalTime;

public record AvailableTimeDto(long id, LocalTime startAt, boolean isBooked) {
}
