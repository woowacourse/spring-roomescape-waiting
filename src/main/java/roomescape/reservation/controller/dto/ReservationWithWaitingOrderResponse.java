package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationWithWaitingOrderResponse(
        Long id,
        String name,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Status status,
        Integer waitingOrder,
        String orderId,
        Long amount,
        String paymentKey
) {
    public static ReservationWithWaitingOrderResponse from(ReservationWithWaitingOrder reservation) {
        return new ReservationWithWaitingOrderResponse(
                reservation.id(),
                reservation.name(),
                reservation.time(),
                reservation.theme(),
                reservation.status(),
                reservation.waitingOrder(),
                reservation.orderId(),
                reservation.amount(),
                reservation.paymentKey()
        );
    }
}
