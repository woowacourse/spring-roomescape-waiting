package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.PaymentConfigResponse;

@RestController
@RequestMapping("/payments/config")
public class PaymentConfigController {

    private final String clientKey;

    public PaymentConfigController(@Value("${payment.toss.client-key}") String clientKey) {
        this.clientKey = clientKey;
    }

    @GetMapping
    public ResponseEntity<PaymentConfigResponse> readConfig() {
        return ResponseEntity.ok(new PaymentConfigResponse(clientKey));
    }
}
