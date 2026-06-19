package roomescape.dto.response;

import java.util.List;
import java.util.Map;
import roomescape.domain.Reservation;
import roomescape.domain.payment.PaymentOrder;

public record ReservationWithStatusResponses(
        List<ReservationResponse> reservations,
        List<WaitingReservationResponse> waitingReservations,
        boolean hasNext
) {
    public static ReservationWithStatusResponses of(
            List<Reservation> reservations,
            Map<Reservation, Integer> waitingReservations,
            Map<Long, PaymentOrder> paymentOrdersByReservationId,
            boolean hasNext
    ) {
        return new ReservationWithStatusResponses(
                reservations.stream()
                        .map(reservation -> ReservationResponse.from(
                                reservation,
                                paymentOrdersByReservationId.get(reservation.getId())))
                        .toList(),
                waitingReservations.entrySet().stream()
                        .map(entry -> WaitingReservationResponse.from(entry.getKey(), entry.getValue()))
                        .toList(),
                hasNext
        );
    }

    public static ReservationWithStatusResponses of(
            List<Reservation> reservations,
            Map<Reservation, Integer> waitingReservations,
            boolean hasNext
    ) {
        return of(reservations, waitingReservations, Map.of(), hasNext);
    }
}
