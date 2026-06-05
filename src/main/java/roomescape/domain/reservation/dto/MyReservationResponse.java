package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationSummary;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        LocalTime time,
        String themeName
) {

    public static MyReservationResponse from(ReservationSummary summary) {
        return new MyReservationResponse(
                summary.id(),
                summary.name(),
                summary.date(),
                summary.startAt(),
                summary.themeName()
        );
    }
}
