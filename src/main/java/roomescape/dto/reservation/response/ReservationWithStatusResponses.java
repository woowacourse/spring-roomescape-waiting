package roomescape.dto.reservation.response;

import java.util.List;
import roomescape.domain.ReservationWithWaitingOrder;

public record ReservationWithStatusResponses(
        List<ReservationResponse> reservations,
        List<WaitingReservationResponse> waitingReservations,
        boolean hasNext
) {
    public static ReservationWithStatusResponses of(
            List<ReservationWithWaitingOrder> reservationWithWaitingOrders, boolean hasNext) {
        List<ReservationResponse> reservations = reservationWithWaitingOrders.stream()
                .filter(reservationWithWaitingOrder -> !reservationWithWaitingOrder.isWaiting())
                .map(reservationWithWaitingOrder ->
                        ReservationResponse.from(reservationWithWaitingOrder.reservation()))
                .toList();
        List<WaitingReservationResponse> waitingReservations = reservationWithWaitingOrders.stream()
                .filter(ReservationWithWaitingOrder::isWaiting)
                .map(reservationWithWaitingOrder -> WaitingReservationResponse.from(
                        reservationWithWaitingOrder.reservation(),
                        reservationWithWaitingOrder.waitingOrder()))
                .toList();
        return new ReservationWithStatusResponses(reservations, waitingReservations, hasNext);
    }
}