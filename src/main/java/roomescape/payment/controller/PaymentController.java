package roomescape.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentConfirmationUncertainException;
import roomescape.payment.exception.PaymentFailureException;
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
        } catch (PaymentConfirmationUncertainException e) {
            // 결과 불명 — 이미 승인됐을 수 있으므로 cleanup 하지 않고 확인을 안내한다.
            model.addAttribute("orderId", orderId);
            return "payment/pending";
        } catch (PaymentFailureException e) {
            paymentService.cleanupByOrderId(orderId);
            return failView(model, e.getCode(), e.getMessage(), orderId);
        }
    }

    @PostMapping("/{orderId}/retry")
    @ResponseBody
    public PaymentResult retry(@PathVariable String orderId) {
        try {
            return paymentService.retryConfirmation(orderId);
        } catch (PaymentFailureException e) {
            paymentService.cleanupByOrderId(orderId);
            throw e;
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
