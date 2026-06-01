package roomescape.schedule.dto.response;

import roomescape.schedule.Schedule;

import java.time.LocalDate;

public record ScheduleSaveResponse(
        long id,
        LocalDate date,
        long time_id,
        long theme_id
) {
    public static ScheduleSaveResponse from(Schedule schedule) {
        return new ScheduleSaveResponse(
                schedule.getId(),
                schedule.getDate(),
                schedule.getTimeId(),
                schedule.getThemeId()
        );
    }
}
