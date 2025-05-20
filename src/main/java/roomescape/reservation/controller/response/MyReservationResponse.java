package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                    @JsonFormat(pattern = "HH:mm") LocalTime time,
                                    String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getStartAt(),
                "예약"
        );
    }

    public static List<MyReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
