package roomescape.feature.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.payment.PaymentProperties;
import roomescape.feature.payment.dto.PaymentConfigResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProperties paymentProperties;

    public PaymentController(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> getPaymentConfig() {
        return ResponseEntity.ok(
                new PaymentConfigResponse(paymentProperties.clientKey(), paymentProperties.amount()));
    }
}
