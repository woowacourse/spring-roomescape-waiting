package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.TossPaymentException;
import roomescape.service.PaymentService;

@Controller
public class CheckoutController {

    private final PaymentService paymentService;
    private final String clientKey;

    public CheckoutController(PaymentService paymentService, @Value("${toss.client-key}") String clientKey) {
        this.paymentService = paymentService;
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
            return "redirect:/?payment=fail&code=AMOUNT_MISMATCH&message=" + e.getMessage();
        } catch (TossPaymentException e) {
            return "redirect:/?payment=fail&code=" + e.getCode() + "&message=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/?payment=fail&code=UNKNOWN&message=" + e.getMessage();
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId
    ) {
        return "redirect:/?payment=fail&code=" + code + "&message=" + message;
    }
}
