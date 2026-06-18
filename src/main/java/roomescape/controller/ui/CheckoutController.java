package roomescape.controller.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.TossPaymentException;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;

@Controller
public class CheckoutController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final String clientKey;

    public CheckoutController(PaymentService paymentService, ReservationService reservationService, @Value("${toss.client-key}") String clientKey) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.clientKey = clientKey;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
            return "redirect:/?payment=success";
        } catch (PaymentAmountMismatchException e) {
            reservationService.cancelByOrderId(orderId);
            return "redirect:/?payment=fail&code=AMOUNT_MISMATCH&message=" + encode(e.getMessage());
        } catch (TossPaymentException.ReadTimeout e) {
            return "redirect:/?payment=uncertain&code=" + e.getCode() + "&message=" + encode(e.getMessage());
        } catch (TossPaymentException e) {
            reservationService.cancelByOrderId(orderId);
            return "redirect:/?payment=fail&code=" + e.getCode() + "&message=" + encode(e.getMessage());
        } catch (Exception e) {
            reservationService.cancelByOrderId(orderId);
            return "redirect:/?payment=fail&code=UNKNOWN&message=" + encode(e.getMessage());
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId
    ) {
        if (orderId != null) {
            reservationService.cancelByOrderId(orderId);
        }
        return "redirect:/?payment=fail&code=" + code + "&message=" + encode(message);
    }

    private String encode(String message) {
        if (message == null) return "";
        return java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
    }
}
