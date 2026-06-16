package roomescape.dto.reservation;

import roomescape.domain.reservationOrder.ReservationOrder;

public record ReserveResponse(String orderId, long amount) {

    public static ReserveResponse from(ReservationOrder reservationOrder) {
        return new ReserveResponse(reservationOrder.getId(), reservationOrder.getAmount());
    }
}
