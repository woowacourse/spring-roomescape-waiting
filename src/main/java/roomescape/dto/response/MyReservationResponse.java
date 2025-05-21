package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.dto.query.WaitingWithRank;
import roomescape.entity.Reservation;
import roomescape.global.ReservationStatus;

public record MyReservationResponse(
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {

        return new MyReservationResponse(
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                ReservationStatus.RESERVED.getText()
        );
    }

    public static MyReservationResponse from(WaitingWithRank waitingWithRank) {

        return new MyReservationResponse(
                waitingWithRank.getWaiting().getThemeName(),
                waitingWithRank.getWaiting().getDate(),
                waitingWithRank.getWaiting().getStartAt(),
                waitingWithRank.getRank() + "번째 " + ReservationStatus.WAIT.getText()
        );
    }
}
