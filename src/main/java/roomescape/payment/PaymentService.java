package roomescape.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.order.Order;
import roomescape.payment.order.OrderRepository;

/**
 * 결제 승인 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
@Transactional
public class PaymentService {

  private final OrderRepository orderRepository;
  private final PaymentGateway paymentGateway;
  private final ApplicationEventPublisher eventPublisher;

  public PaymentService(
      OrderRepository orderRepository,
      PaymentGateway paymentGateway,
      ApplicationEventPublisher eventPublisher
  ) {
    this.orderRepository = orderRepository;
    this.paymentGateway = paymentGateway;
    this.eventPublisher = eventPublisher;
  }

  public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
    Order order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new OrderNotFoundException("요청한 결제 주문을 찾을 수 없습니다."));
    if (!order.amount().equals(amount)) {
      throw new PaymentAmountMismatchException(order.amount(), amount);
    }
    if (order.status() == PaymentStatus.DONE) {
      return new PaymentResult(order.paymentKey(), orderId, order.amount(), order.status());
    }

    PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
    PaymentResult result = paymentGateway.confirm(confirmation);
    orderRepository.confirm(orderId, result.paymentKey(), result.status());
    if (result.status() == PaymentStatus.DONE) {
      eventPublisher.publishEvent(new PaymentConfirmedEvent(order.reservationId()));
    }
    return result;
  }

  public void fail(String code, String message, String orderId) {
    if (orderId == null || orderId.isBlank()) {
      return;
    }

    orderRepository.findByOrderId(orderId)
        .ifPresent(order -> eventPublisher.publishEvent(new PaymentFailedEvent(order.reservationId())));
  }

}
