package roomescape.domain.reservation.dto;

import java.util.List;
import java.util.Map;
import roomescape.domain.payment.ReservationPayment;
import roomescape.domain.reservation.Reservation;

public record MyReservationsResponse(
    List<MyReservationResponse> reservations
) {

    public static MyReservationsResponse from(
        List<Reservation> reservations,
        Map<Long, ReservationPayment> paymentsByReservationId
    ) {
        return new MyReservationsResponse(reservations.stream()
            .map(reservation -> MyReservationResponse.from(
                reservation,
                paymentsByReservationId.get(reservation.getId())
            ))
            .toList()
        );
    }
}
