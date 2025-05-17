package roomescape.presentation.api.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.domain.reservation.ReservationStatus;

public record ReservationWithStatusResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static ReservationWithStatusResponse from(ReservationWithStatusResult reservationWithStatusResult) {
        return new ReservationWithStatusResponse(
                reservationWithStatusResult.reservationId(),
                reservationWithStatusResult.themeName(),
                reservationWithStatusResult.reservationDate(),
                reservationWithStatusResult.reservationTime(),
                toDisplayStatus(reservationWithStatusResult.status())
        );
    }

    private static String toDisplayStatus(ReservationStatus status) {
        return switch (status) {
            case RESERVE -> "예약";
        };
    }
}
