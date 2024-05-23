package roomescape.domain.time.dto;

import java.time.LocalDate;

public record BookableTimesRequest(LocalDate date, Long themeId) {
}
