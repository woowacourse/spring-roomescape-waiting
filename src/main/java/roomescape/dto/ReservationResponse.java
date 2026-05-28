package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.ReservationTime;

public record ReservationResponse(
        Long reservationId,
        String name,
        String status,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int order) {

    public static ReservationResponse from(Reservation reservation, Schedule schedule, int order) {
        Theme theme = schedule.getTheme();
        ReservationTime reservationTime = schedule.getTime();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().toString(),
                schedule.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservationTime.getStartAt(),
                order
        );
    }
}
