package roomescape.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.facade.command.ReservationOrderCommand;
import roomescape.application.facade.result.ReservationOrderResult;
import roomescape.application.service.OrderService;
import roomescape.application.service.ReservationService;
import roomescape.application.service.command.OrderPendingCommand;
import roomescape.application.service.result.OrderResult;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.domain.order.OrderAmount;
import roomescape.domain.order.OrderName;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationOrderFacade {

    private final OrderService orderService;
    private final ReservationService reservationService;

    @Transactional
    public ReservationOrderResult createOrder(ReservationOrderCommand command) {
        ReservationSlotResult reservation = reservationService.createPending(command.toReservationPendingCommand());
        OrderResult order = orderService.createPendingOrder(toOrderPendingCommand(command, reservation));
        return ReservationOrderResult.from(order);
    }

    private OrderPendingCommand toOrderPendingCommand(
            ReservationOrderCommand command,
            ReservationSlotResult reservation
    ) {
        return new OrderPendingCommand(
                reservation.reservation().id(),
                command.orderType(),
                OrderName.from(reservation.theme().name(), command.orderType()),
                OrderAmount.valueOf(reservation.theme().price())
        );
    }
}
