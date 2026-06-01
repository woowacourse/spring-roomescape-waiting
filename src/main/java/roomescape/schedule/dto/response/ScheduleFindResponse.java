package roomescape.schedule.dto.response;

import roomescape.schedule.Schedule;

import java.time.LocalDate;
import java.util.List;

public record ScheduleFindResponse(
        Long id,
        LocalDate date,
        Long theme_id,
        Long time_id
) {
    public static ScheduleFindResponse from(Schedule schedule) {
        return new ScheduleFindResponse(
                schedule.getId(),
                schedule.getDate(),
                schedule.getThemeId(),
                schedule.getTimeId()
        );
    }

    public static List<ScheduleFindResponse> from(List<Schedule> schedules) {
        return schedules.stream()
                .map(ScheduleFindResponse::from)
                .toList();
    }
}
