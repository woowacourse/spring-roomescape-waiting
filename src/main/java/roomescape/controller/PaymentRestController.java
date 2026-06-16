package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.payment.PaymentPrepareRequest;
import roomescape.service.PaymentService;

@RestController
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/prepare")
    public ResponseEntity<Void> prepare(@RequestBody PaymentPrepareRequest request) {
        paymentService.prepare(request);
        return ResponseEntity.ok().build();
    }
}