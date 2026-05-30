package roomescape.controller.dto.response;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationResult;

import java.util.List;

public class ReservationResponses {

    private final List<ReservationResponse> reservations;

    public ReservationResponses(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public static ReservationResponses toDto(List<ReservationResult> reservationResultss) {
        return new ReservationResponses(reservationResultss.stream()
                .map(ReservationResponse::toDto)
                .toList());
    }

    public List<ReservationResponse> getReservations() {
        return reservations;
    }
}
