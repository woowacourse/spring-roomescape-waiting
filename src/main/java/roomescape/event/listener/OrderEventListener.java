package roomescape.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.ReservationService;
import roomescape.domain.ReservationStatus;
import roomescape.domain.order.OrderType;
import roomescape.domain.order.event.OrderFailedEvent;
import roomescape.domain.order.event.OrderPaidEvent;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ReservationService reservationService;

    @EventListener
    @Transactional
    public void handle(OrderPaidEvent event) {
        reservationService.confirmPending(event.reservationId(), resolveStatus(event.orderType()));
    }

    @EventListener
    @Transactional
    public void handle(OrderFailedEvent event) {
        reservationService.cancelReservation(event.reservationId());
    }

    private ReservationStatus resolveStatus(OrderType orderType) {
        return switch (orderType) {
            case RESERVATION -> ReservationStatus.RESERVED;
            case WAITING -> ReservationStatus.WAITING;
        };
    }
}
