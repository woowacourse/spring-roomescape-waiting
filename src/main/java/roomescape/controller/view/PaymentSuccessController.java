package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.service.payment.PaymentAmountMismatchException;
import roomescape.service.payment.PaymentGatewayException;
import roomescape.service.payment.PaymentResult;
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
            @RequestParam(required = false) String name,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            model.addAttribute("name", name);
            return "payment-success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId, name);
        } catch (PaymentGatewayException e) {
            return failView(model, e.getCode(), e.getMessage(), orderId, name);
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) String name,
            Model model
    ) {
        if (paymentId != null) {
            paymentService.fail(paymentId, code, message);
        }
        return failView(model, code, message, orderId, name);
    }

    private String failView(Model model, String code, String message, String orderId, String name) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        model.addAttribute("name", name);
        return "payment-fail";
    }
}
