package roomescape.service.dto;

import roomescape.domain.Reservation;

public record UserReservation(
        Reservation reservation,
        ReservationPayment payment
) {
}
