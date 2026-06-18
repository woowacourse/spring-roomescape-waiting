package roomescape.payment.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.application.PaymentService;
import roomescape.payment.dto.PaymentConfirmRequest;
import roomescape.payment.dto.PaymentConfirmResponse;
import roomescape.payment.dto.PaymentFailRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ResponseEntity.ok(paymentService.confirm(request));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestBody PaymentFailRequest request
    ) {
        paymentService.fail(request.orderId());
        return ResponseEntity.ok().build();
    }
}
