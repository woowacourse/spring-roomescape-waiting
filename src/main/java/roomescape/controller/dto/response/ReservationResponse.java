package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public record ReservationResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        ReservationStatus reservationStatus
) {
    public static ReservationResponse from(
            Reservation reservation, Theme theme, ReservationStatus reservationStatus) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                reservationStatus
        );
    }

    public static ReservationResponse fromReserved(Reservation reservation, Theme theme) {
        return from(reservation, theme, ReservationStatus.CONFIRMED);
    }

    public static ReservationResponse fromWaiting(Reservation reservation, Theme theme) {
        return from(reservation, theme, ReservationStatus.WAITING);
    }
}
