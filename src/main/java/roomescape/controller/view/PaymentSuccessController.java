package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentResult;
import roomescape.service.PaymentService;

@Controller
public class PaymentSuccessController {

    private final PaymentService paymentService;

    public PaymentSuccessController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            return "payment-success";
        } catch (PaymentAmountMismatchException e) {
            model.addAttribute("code", "AMOUNT_MISMATCH");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("orderId", orderId);
            return "payment-fail";
        }
    }
}
