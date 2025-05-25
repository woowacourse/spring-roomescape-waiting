package roomescape.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWithWaitingResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MyReservationWithWaitingResponse fromReservation(MyReservationResponse reservation) {
        return new MyReservationWithWaitingResponse(
                reservation.id(),
                reservation.theme(),
                reservation.date(),
                reservation.time(),
                ReservationStatus.RESERVED.getName()
        );
    }

    public static MyReservationWithWaitingResponse fromWaiting(WaitingWithRank waiting) {
        return new MyReservationWithWaitingResponse(
                waiting.id(),
                waiting.themeName(),
                waiting.date(),
                waiting.time(),
                waiting.rank() + "번째 " + ReservationStatus.WAITING.getName()
        );
    }
}
