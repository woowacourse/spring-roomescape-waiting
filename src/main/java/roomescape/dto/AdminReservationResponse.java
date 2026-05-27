package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Schedule;
import roomescape.domain.Theme;

public record AdminReservationResponse(
        Long id,
        LocalDate date,
        String themeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {

    public static AdminReservationResponse from(Schedule schedule, Theme theme) {
        return new AdminReservationResponse(
                schedule.getId(),
                schedule.getDate(),
                theme.getName(),
                schedule.getTime().getStartAt()
        );
    }
}
