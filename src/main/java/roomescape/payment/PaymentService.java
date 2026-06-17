package roomescape.payment;

import java.util.UUID;
import org.springframework.stereotype.Service;
import roomescape.global.NotFoundException;
import roomescape.payment.order.Order;
import roomescape.payment.order.OrderRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationErrorMessage;

/**
 * 결제 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
public class PaymentService {

  private final OrderRepository orderRepository;
  private final PaymentGateway paymentGateway;
  private final ReservationRepository reservationRepository;

  public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway,
      ReservationRepository reservationRepository) {
    this.orderRepository = orderRepository;
    this.paymentGateway = paymentGateway;
    this.reservationRepository = reservationRepository;
  }

  public CheckoutInfo prepareCheckout(Long reservationId) {
    var detail = reservationRepository.findDetailById(reservationId)
        .orElseThrow(() -> new NotFoundException(ReservationErrorMessage.RESERVATION_NOT_FOUND, reservationId));
    var orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
    orderRepository.save(new Order(orderId, detail.amount(), reservationId));
    return new CheckoutInfo(orderId, detail.amount(), detail.themeName());
  }

  public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
    var order = orderRepository.getByOrderId(orderId);
    if (!order.getAmount().equals(amount)) {
      throw new PaymentAmountMismatchException(order.getAmount(), amount);
    }
    var result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
    orderRepository.updatePaymentKey(orderId, result.paymentKey());
    reservationRepository.confirmPayment(order.getReservationId());
    return result;
  }

  public void handleTimeout(String orderId) {
    var order = orderRepository.getByOrderId(orderId);
    reservationRepository.markAsUncertain(order.getReservationId());
  }

  public String getReservationName(String orderId) {
    var order = orderRepository.getByOrderId(orderId);
    return reservationRepository.findById(order.getReservationId())
        .orElseThrow(() -> new IllegalStateException("결제 완료 후 예약을 찾을 수 없음: " + order.getReservationId()))
        .getName();
  }

}
