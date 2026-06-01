package roomescape.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.schedule.Schedule;

import java.time.LocalDate;

public record ScheduleSaveRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
    public Schedule toDomain() {
        return new Schedule(
                null,
                date,
                timeId,
                themeId
        );
    }
}
