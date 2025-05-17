package roomescape.presentation.dto.response;

import roomescape.domain.Reservation;

import java.util.List;

public record MyReservationResponse(Long reservationId, String theme, String date, String time, String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString(),
                reservation.getStatus().getName()
        );
    }

    public static List<MyReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
