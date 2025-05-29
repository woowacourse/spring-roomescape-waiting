package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public record MyReservationResponse(
        Long id,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getStartAt(),
                "예약"
        );
    }

    public static MyReservationResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();

        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getReservationDatetime().reservationDate().date(),
                waiting.getReservationDatetime().reservationTime().getStartAt(),
                String.format("%d번째 예약 대기", waitingWithRank.getRank() + 1)
        );
    }
}
