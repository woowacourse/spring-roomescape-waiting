package roomescape.dto.reservationOrder;

import roomescape.domain.reservationOrder.ReservationOrder;

public record OrderResponse(Long id, String orderId, long amount) {

    public static OrderResponse from(ReservationOrder reservationOrder) {
        return new OrderResponse(reservationOrder.getReservationId(), reservationOrder.getId(), reservationOrder.getAmount());
    }
}
