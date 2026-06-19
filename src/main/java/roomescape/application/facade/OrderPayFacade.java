package roomescape.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.OrderService;
import roomescape.application.service.PaymentService;
import roomescape.application.service.command.PaymentConfirmCommand;
import roomescape.application.service.result.PayableOrderResult;
import roomescape.application.service.result.PaymentApprovalResult;
import roomescape.pg.PaymentResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPayFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public PaymentApprovalResult confirm(PaymentConfirmCommand command) {
        PayableOrderResult order = orderService.getPayableOrder(command.orderId());
        PaymentResult payment = paymentService.confirm(command, order.amount().value());
        return new PaymentApprovalResult(payment, order.orderType(), order.targetId());
    }
}
