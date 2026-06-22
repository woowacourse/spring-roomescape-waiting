package roomescape.controller.dto.response;

import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservation.Reservations;

import java.util.List;

public class ReservationResponses {

    private final List<ReservationResponse> reservations;

    public ReservationResponses(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public static ReservationResponses toDto(Reservations reservations) {
        return new ReservationResponses(reservations.getValues().stream()
                .map(ReservationResponse::toDto)
                .toList());
    }

    public static ReservationResponses toDtoWithRank(List<ReservationWithRank> reservations) {
        return new ReservationResponses(reservations.stream()
                .map(ReservationResponse::toDto)
                .toList());
    }

    public List<ReservationResponse> getReservations() {
        return reservations;
    }
}
