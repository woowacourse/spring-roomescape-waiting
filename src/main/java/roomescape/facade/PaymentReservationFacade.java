package roomescape.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;
import roomescape.service.dto.result.PaymentResult;

@Component
@Transactional(readOnly = true)
public class PaymentReservationFacade {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final OrderRepository orderRepository;

    public PaymentReservationFacade(
            PaymentService paymentService,
            ReservationService reservationService,
            OrderRepository orderRepository
    ) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.orderRepository = orderRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentResult confirmPaymentAndReservation(String paymentKey, String orderId, Long amount) {
        PaymentResult paymentResult = paymentService.confirm(paymentKey, orderId, amount);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다."));

        reservationService.confirmReservation(order.reservation_id());

        return paymentResult;
    }
}
