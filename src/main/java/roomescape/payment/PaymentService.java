package roomescape.payment;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
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

  @Transactional(noRollbackFor = PaymentGatewayNoResponseException.class)
  public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
    Order order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new OrderNotFoundException("요청한 결제 주문을 찾을 수 없습니다."));
    if (!order.amount().equals(amount)) {
      throw new PaymentAmountMismatchException(order.amount(), amount);
    }
    if (order.status() == PaymentStatus.DONE) {
      return new PaymentResult(order.paymentKey(), orderId, order.amount(), order.status());
    }

    PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount, order.idempotencyKey());
    PaymentResult result;
    try {
      result = paymentGateway.confirm(confirmation);
    } catch (RestClientException e) {
      if (isReadTimeout(e)) {
        orderRepository.markUnknown(orderId);
        throw new PaymentGatewayNoResponseException("결제 승인 응답을 받지 못했습니다. 내 예약에서 결제 상태를 확인하거나 다시 시도해주세요.", e);
      }
      if (isConnectionFailure(e)) {
        throw new PaymentGatewayConnectionException("결제 승인 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.", e);
      }
      throw e;
    }
    orderRepository.confirm(orderId, result.paymentKey(), result.status());
    if (result.status() == PaymentStatus.DONE) {
      eventPublisher.publishEvent(new PaymentConfirmedEvent(order.reservationId()));
    }
    return result;
  }

  private boolean isReadTimeout(RestClientException exception) {
    Throwable root = rootCause(exception);
    if (!(root instanceof SocketTimeoutException)) {
      return false;
    }
    String message = root.getMessage();
    return message == null || !message.toLowerCase().contains("connect");
  }

  private boolean isConnectionFailure(RestClientException exception) {
    Throwable root = rootCause(exception);
    if (root instanceof ConnectException) {
      return true;
    }
    if (root instanceof SocketTimeoutException) {
      String message = root.getMessage();
      return message != null && message.toLowerCase().contains("connect");
    }
    return false;
  }

  private Throwable rootCause(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current;
  }

  public void fail(String code, String message, String orderId) {
    if (orderId == null || orderId.isBlank()) {
      return;
    }

    orderRepository.findByOrderId(orderId)
        .ifPresent(order -> eventPublisher.publishEvent(new PaymentFailedEvent(order.reservationId())));
  }

}
