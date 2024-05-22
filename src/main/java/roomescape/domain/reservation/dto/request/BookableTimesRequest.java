package roomescape.domain.reservation.dto.request;

import java.time.LocalDate;

public record BookableTimesRequest(LocalDate date, Long themeId) {
}
