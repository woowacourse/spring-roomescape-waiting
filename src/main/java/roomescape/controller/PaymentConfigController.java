package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.PaymentConfigResponse;

@RestController
@RequestMapping("/payments")
public class PaymentConfigController {
    private final String clientKey;

    public PaymentConfigController(@Value("${toss.client-key}") String clientKey) {
        this.clientKey = clientKey;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> config() {
        return ResponseEntity.ok(new PaymentConfigResponse(clientKey));
    }
}
