package roomescape.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {
    public static MyReservationResponse of(Reservation reservation, long rank) {
        return new MyReservationResponse(
                reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt(), getWaitingOrder(reservation.getStatus(), rank)
        );
    }

    private static String getWaitingOrder(Status status, long rank) {
        if (status == Status.RESERVATION) {
            return status.getValue();
        }
        return rank + "번째 " + status.getValue();
    }
}
