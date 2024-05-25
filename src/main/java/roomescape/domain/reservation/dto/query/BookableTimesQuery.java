package roomescape.domain.reservation.dto.query;

import java.time.LocalDate;

public record BookableTimesQuery(LocalDate date, Long themeId) {
}
