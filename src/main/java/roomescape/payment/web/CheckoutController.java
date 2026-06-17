package roomescape.payment.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentService;
import roomescape.payment.client.TossPaymentException;
import roomescape.payment.order.Order;
import roomescape.payment.order.OrderRepository;
import roomescape.global.NotFoundException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationErrorMessage;

/**
 * 브라우저에서 Toss 결제위젯으로 결제 흐름 전체(인증 → 승인)를 체험하는 SSR(Thymeleaf) 컨트롤러. 미구현(initial) 상태에서는 위젯 인증까지만 되고 승인 결과가 채워지지 않는다.
 */
@Controller
public class CheckoutController {

  private final OrderRepository orderRepository;
  private final PaymentService paymentService;
  private final ReservationRepository reservationRepository;
  private final String clientKey;

  public CheckoutController(
      OrderRepository orderRepository,
      PaymentService paymentService,
      ReservationRepository reservationRepository,
      @Value("${toss.client-key:}") String clientKey
  ) {
    this.orderRepository = orderRepository;
    this.paymentService = paymentService;
    this.reservationRepository = reservationRepository;
    this.clientKey = clientKey;
  }

  @GetMapping("/payment")
  public String checkout(@RequestParam Long reservationId, Model model) {
    var detail = reservationRepository.findDetailById(reservationId)
        .orElseThrow(() -> new NotFoundException(ReservationErrorMessage.RESERVATION_NOT_FOUND, reservationId));

    var orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
    orderRepository.save(new Order(orderId, detail.amount(), reservationId));

    model.addAttribute("clientKey", clientKey);
    model.addAttribute("orderId", orderId);
    model.addAttribute("orderName", "방탈출 예약 — " + detail.themeName());
    model.addAttribute("amount", detail.amount());
    return "checkout";
  }

  @GetMapping("/payments/success")
  public String success(
      @RequestParam String paymentKey,
      @RequestParam String orderId,
      @RequestParam Long amount,
      Model model
  ) {
    try {
      var result = paymentService.confirm(paymentKey, orderId, amount);
      var order = orderRepository.getByOrderId(orderId);
      reservationRepository.confirmPayment(order.getReservationId());
      var reservation = reservationRepository.findById(order.getReservationId())
          .orElseThrow(() -> new NotFoundException(ReservationErrorMessage.RESERVATION_NOT_FOUND, order.getReservationId()));
      var encodedName = URLEncoder.encode(reservation.getName(), StandardCharsets.UTF_8);
      model.addAttribute("result", result);
      model.addAttribute("reservationUrl", "/?name=" + encodedName + "#/reservations");
      return "success";
    } catch (PaymentAmountMismatchException e) {
      return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
    } catch (TossPaymentException e) {
      return failView(model, e.getCode(), e.getMessage(), orderId);
    }
  }

  @GetMapping("/payments/fail")
  public String fail(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String message,
      @RequestParam(required = false) String orderId,
      Model model
  ) {
    // 사용자 취소 시 orderId 가 없을 수 있다.
    return failView(model, code, message, orderId);
  }

  private String failView(Model model, String code, String message, String orderId) {
    model.addAttribute("code", code);
    model.addAttribute("message", message);
    model.addAttribute("orderId", orderId);
    return "fail";
  }

}
