package roomescape.presentation.dto.response;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MyReservationResponse(String theme, LocalDate date, LocalTime time, String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getName()
        );
    }

    public static List<MyReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
