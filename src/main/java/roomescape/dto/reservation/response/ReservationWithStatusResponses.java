package roomescape.dto.reservation.response;

import java.util.List;
import roomescape.domain.ReservationWithWaitingOrder;

public record ReservationWithStatusResponses(
        List<ReservationResponse> reservations,
        List<WaitingReservationResponse> waitingReservations,
        boolean hasNext
) {
    public static ReservationWithStatusResponses of(List<ReservationWithWaitingOrder> rows, boolean hasNext) {
        List<ReservationResponse> reservations = rows.stream()
                .filter(ReservationWithWaitingOrder::isReserved)
                .map(row -> ReservationResponse.from(row.reservation()))
                .toList();
        List<WaitingReservationResponse> waitingReservations = rows.stream()
                .filter(ReservationWithWaitingOrder::isWaiting)
                .map(row -> WaitingReservationResponse.from(row.reservation(), row.waitingOrder()))
                .toList();
        return new ReservationWithStatusResponses(reservations, waitingReservations, hasNext);
    }
}