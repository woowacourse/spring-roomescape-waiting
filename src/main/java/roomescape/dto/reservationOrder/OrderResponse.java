package roomescape.dto.reservationOrder;

import roomescape.domain.reservationOrder.ReservationOrder;

public record OrderResponse(String orderId, long amount) {

    public static OrderResponse from(ReservationOrder reservationOrder) {
        return new OrderResponse(reservationOrder.getId(), reservationOrder.getAmount());
    }
}
