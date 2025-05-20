package roomescape.presentation.api.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.application.reservation.query.dto.ReservationWithStatusResult;
import roomescape.application.reservation.query.dto.WaitingResult;
import roomescape.domain.reservation.ReservationStatus;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        ReservationResponseType type
) {

    public static MyReservationResponse from(ReservationWithStatusResult reservationWithStatusResult) {
        return new MyReservationResponse(
                reservationWithStatusResult.reservationId(),
                reservationWithStatusResult.themeName(),
                reservationWithStatusResult.reservationDate(),
                reservationWithStatusResult.reservationTime(),
                toDisplayStatus(reservationWithStatusResult.status()),
                ReservationResponseType.RESERVE
        );
    }

    public static MyReservationResponse from(WaitingResult waitingResult) {
        return new MyReservationResponse(
                waitingResult.waitingId(),
                waitingResult.themeName(),
                waitingResult.reservationDate(),
                waitingResult.reservationTime(),
                toDisplayStatus(waitingResult.waitingCount()),
                ReservationResponseType.WAITING
        );
    }

    private static String toDisplayStatus(ReservationStatus status) {
        return switch (status) {
            case RESERVE -> "예약";
        };
    }

    private static String toDisplayStatus(long waitingCount) {
        return waitingCount + "번째 예약 대기";
    }
}
