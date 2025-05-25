package roomescape.presentation.dto.response;

import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWithWaitingResponse(
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyReservationWithWaitingResponse fromReservation(MyReservationResponse reservation) {
        return new MyReservationWithWaitingResponse(
                reservation.theme(),
                reservation.date(),
                reservation.time(),
                ReservationStatus.RESERVED.getName()
        );
    }

    public static MyReservationWithWaitingResponse fromWaiting(WaitingWithRank waiting) {
        return new MyReservationWithWaitingResponse(
                waiting.themeName(),
                waiting.date(),
                waiting.time(),
                waiting.rank() + "번째 " + ReservationStatus.WAITING.getName()
        );
    }
}
