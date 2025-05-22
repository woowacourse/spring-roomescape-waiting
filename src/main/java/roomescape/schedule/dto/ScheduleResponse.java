package roomescape.schedule.dto;

import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.schedule.Schedule;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;

public record ScheduleResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ScheduleResponse of(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getDate(),
                ReservationTimeResponse.from(schedule.getReservationTime()),
                ThemeResponse.from(schedule.getTheme())
        );
    }
}
