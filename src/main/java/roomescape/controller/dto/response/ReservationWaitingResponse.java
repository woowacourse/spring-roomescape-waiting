package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWaitingResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        long waitingNumber,
        ReservationStatus reservationStatus
) {
    public static ReservationWaitingResponse from(Reservation reservation, Theme theme, long waitingNumber) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                waitingNumber,
                ReservationStatus.WAITING
        );
    }
}
