package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.PaymentResult;
import roomescape.dto.response.PaymentConfirmResponse;
import roomescape.dto.response.PaymentFailResponse;
import roomescape.service.PaymentService;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
        return ResponseEntity.ok(PaymentConfirmResponse.from(result));
    }

    @GetMapping("/payments/fail")
    public ResponseEntity<PaymentFailResponse> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.fail(orderId);
        return ResponseEntity.ok(new PaymentFailResponse(code, message, orderId));
    }
}
