package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.PaymentConfirmResponse;
import roomescape.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/success")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        return ResponseEntity.ok(PaymentConfirmResponse.from(
                paymentService.confirmPayment(paymentKey, orderId, amount)
        ));
    }
}
