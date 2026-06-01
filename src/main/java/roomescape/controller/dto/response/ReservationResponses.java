package roomescape.controller.dto.response;

import roomescape.domain.reservation.Reservation;
import java.util.List;

public class ReservationResponses {

    private final List<ReservationResponse> reservations;

    public ReservationResponses(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public static ReservationResponses toDto(List<Reservation> reservations) {
        return new ReservationResponses(reservations.stream()
                .map(ReservationResponse::toDto)
                .toList());
    }

    public List<ReservationResponse> getReservations() {
        return reservations;
    }
}
