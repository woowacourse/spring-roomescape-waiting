package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.Time;

public record ReservationResponse(
        long reservationId,
        String name,
        String status,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int order) {
    public static ReservationResponse from(Reservation reservation, ReservationSlot reservationSlot, int order) {
        Theme theme = reservationSlot.getTheme();
        Time time = reservationSlot.getTime();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().toString(),
                reservationSlot.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                time.getStartAt(),
                order
        );
    }
}
