package roomescape.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ScheduleRequest(
        @NotNull LocalDate date,
        @NotNull Long reservationTimeId,
        @NotNull Long themeId
) {
}
