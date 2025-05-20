package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.WaitingWithRank;

public record MyReservationResponse(String theme,
                                    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                    @JsonFormat(pattern = "HH:mm") LocalTime time,
                                    String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getStartAt(),
                "예약"
        );
    }

    public static MyReservationResponse from(WaitingWithRank waitingWithRank) {
        ReservationWaiting waiting = waitingWithRank.reservationWaiting();
        return new MyReservationResponse(
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getStartAt(),
                String.valueOf(waitingWithRank.rank())
        );
    }


    public static List<MyReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
