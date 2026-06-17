package roomescape.payment.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentService;

@Controller
public class CheckoutController {

  private final PaymentService paymentService;
  private final String clientKey;

  public CheckoutController(
      PaymentService paymentService,
      @Value("${toss.client-key:}") String clientKey
  ) {
    this.paymentService = paymentService;
    this.clientKey = clientKey;
  }

  @GetMapping("/payment")
  public String checkout(@RequestParam Long reservationId, Model model) {
    var info = paymentService.prepareCheckout(reservationId);
    model.addAttribute("clientKey", clientKey);
    model.addAttribute("orderId", info.orderId());
    model.addAttribute("orderName", "방탈출 예약 — " + info.themeName());
    model.addAttribute("amount", info.amount());
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
      model.addAttribute("result", result);
      model.addAttribute("reservationUrl", reservationListUrl(orderId));
      return "success";
    } catch (PaymentAmountMismatchException e) {
      return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
    } catch (RestClientResponseException e) {
      return failView(model, "PAYMENT_FAILED", "결제 처리 중 오류가 발생했습니다.", orderId);
    } catch (ResourceAccessException e) {
      return failView(model, "CONNECTION_FAILED", "토스 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.", orderId);
    } catch (RestClientException e) {
      // read timeout — 결제 결과 불명확, UNCERTAIN 처리 후 예약 목록으로
      paymentService.handleTimeout(orderId);
      return "redirect:" + reservationListUrl(orderId);
    }
  }

  @GetMapping("/payments/fail")
  public String fail(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String message,
      @RequestParam(required = false) String orderId,
      Model model
  ) {
    return failView(model, code, message, orderId);
  }

  private String reservationListUrl(String orderId) {
    var name = paymentService.getReservationName(orderId);
    return "/?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "#/reservations";
  }

  private String failView(Model model, String code, String message, String orderId) {
    model.addAttribute("code", code);
    model.addAttribute("message", message);
    model.addAttribute("orderId", orderId);
    return "fail";
  }

}
