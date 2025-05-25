package roomescape.presentation.api.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.application.reservation.dto.WaitingWitStatusResult;
import roomescape.domain.reservation.ReservationStatus;

public record ReservationWithStatusResponse(
        Long id,
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

    public static ReservationWithStatusResponse from(WaitingWitStatusResult waitingWitStatusResult) {
        return new ReservationWithStatusResponse(
                waitingWitStatusResult.waitingId(),
                waitingWitStatusResult.theme(),
                waitingWitStatusResult.date(),
                waitingWitStatusResult.time(),
                toDisplayStatus(waitingWitStatusResult.rank())
        );
    }

    private static String toDisplayStatus(ReservationStatus status) {
        return switch (status) {
            case RESERVE -> "예약";
        };
    }

    private static String toDisplayStatus(long rank) {
        return rank + "번째 예약대기";
    }
}
