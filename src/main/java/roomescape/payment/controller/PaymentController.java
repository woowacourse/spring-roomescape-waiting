package roomescape.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.infrastructure.TossPaymentException;
import roomescape.payment.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(PaymentService paymentService, @Value("${toss.client-key:}") String clientKey) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam String orderId, Model model) {
        var checkoutInfo = paymentService.getCheckoutInfo(orderId);
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", checkoutInfo.orderId());
        model.addAttribute("amount", checkoutInfo.amount());
        model.addAttribute("orderName", checkoutInfo.reservationName());
        return "payment/checkout";
    }

    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            model.addAttribute("result", paymentService.confirm(paymentKey, orderId, amount));
            model.addAttribute("paymentKey", paymentKey);
            return "payment/success";
        } catch (PaymentAmountMismatchException e) {
            paymentService.cleanupByOrderId(orderId);
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            paymentService.cleanupByOrderId(orderId);
            return failView(model, e.getCode(), e.getMessage(), orderId);
        }
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        paymentService.cleanupByOrderId(orderId);
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
