package roomescape.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.ReservationTime;

public record ReservationResponse(
        Long reservationId,
        String name,
        DisplayStatus status,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int order
) {

    public static ReservationResponse from(Reservation reservation, int order, LocalDateTime now) {
        Schedule schedule = reservation.getSchedule();
        Theme theme = schedule.getTheme();
        ReservationTime reservationTime = schedule.getTime();

        LocalDateTime reservationDateTime = LocalDateTime.of(
                schedule.getDate(),
                reservationTime.getStartAt()
        );

        DisplayStatus displayStatus = DisplayStatus.from(
                reservation.getStatus(),
                now,
                reservationDateTime
        );

        return new ReservationResponse(
                reservation.getId(),
                reservation.getReserver().getName(),
                displayStatus,
                schedule.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservationTime.getStartAt(),
                order
        );
    }
}
