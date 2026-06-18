package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.payment.PaymentResult;
import roomescape.infrastructure.LoginRequired;
import roomescape.service.PaymentService;

@RestController
@RequestMapping("/payments")
@LoginRequired
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/success")
    public ResponseEntity<PaymentResult> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        PaymentResult paymentResult = paymentService.confirm(paymentKey, orderId, amount);
        return ResponseEntity.ok(paymentResult);
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(@RequestParam(required = false) String orderId) {
        paymentService.cancelPendingOrder(orderId);
        return ResponseEntity.ok().build();
    }
}