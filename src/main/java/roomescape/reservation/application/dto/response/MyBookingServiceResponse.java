package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.vo.WaitingWithRank;

public record MyBookingServiceResponse(
    Long reservationId,
    String themeName,
    LocalDate date,
    LocalTime time,
    String status
) {

    public static MyBookingServiceResponse from(Reservation reservation) {
        return new MyBookingServiceResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            "예약"
        );
    }

    public static MyBookingServiceResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        Long rank = waitingWithRank.getRank();
        return new MyBookingServiceResponse(
            waiting.getId(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getTime().getStartAt(),
            String.format("%d번째 예약 대기", rank + 1)
        );
    }
}
