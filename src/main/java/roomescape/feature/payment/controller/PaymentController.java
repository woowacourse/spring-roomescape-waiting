package roomescape.feature.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.payment.dto.PaymentConfigResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final String clientKey;
    private final Long amount;

    public PaymentController(
            @Value("${toss.payments.client-key}") String clientKey,
            @Value("${toss.payments.amount}") Long amount
    ) {
        this.clientKey = clientKey;
        this.amount = amount;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> getPaymentConfig() {
        return ResponseEntity.ok(new PaymentConfigResponse(clientKey, amount));
    }
}
