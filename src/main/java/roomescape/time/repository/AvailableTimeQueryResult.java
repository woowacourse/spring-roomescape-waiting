package roomescape.time.repository;

import java.time.LocalTime;

public record AvailableTimeQueryResult(Long id, LocalTime startAt) {
}
