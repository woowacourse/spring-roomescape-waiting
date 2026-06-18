package roomescape.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.OrderService;
import roomescape.domain.payment.event.PaymentApprovedEvent;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @EventListener
    @Transactional
    public void handle(PaymentApprovedEvent event) {
        orderService.payOrder(event.orderId());
    }
}
