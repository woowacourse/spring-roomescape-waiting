package roomescape.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(),
                reservation.getStartAt(),
                ReservationStatus.예약.getDisplay()
        );
    }

    public static MyReservationResponse fromWaiting(final WaitingWithRank waitingWithRank) {
        final Waiting waiting = waitingWithRank.waiting();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName().name(),
                waiting.getDate(),
                waiting.getStartAt(),
                waitingWithRank.rank() + 1 + "번째 " + ReservationStatus.대기.getDisplay()
        );
    }
}
