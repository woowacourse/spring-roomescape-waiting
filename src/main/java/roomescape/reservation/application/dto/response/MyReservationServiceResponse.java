package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.vo.WaitingWithRank;

public record MyReservationServiceResponse(
    Long reservationId,
    String themeName,
    LocalDate date,
    LocalTime time,
    String status
) {

    public static MyReservationServiceResponse from(Reservation reservation) {
        return new MyReservationServiceResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            "예약"
        );
    }

    public static MyReservationServiceResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        Long rank = waitingWithRank.getRank();
        return new MyReservationServiceResponse(
            waiting.getId(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getTime().getStartAt(),
            String.format("%d번째 예약 대기", rank + 1)
        );
    }
}
