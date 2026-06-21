package roomescape.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.payment.controller.dto.request.PaymentConfirmRequest;
import roomescape.payment.controller.dto.request.PaymentFailRequest;
import roomescape.payment.controller.dto.response.PaymentConfigResponse;
import roomescape.payment.controller.dto.response.PaymentConfirmResponse;
import roomescape.payment.controller.dto.response.PaymentFailResponse;
import roomescape.payment.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> getConfig() {
        return ResponseEntity.ok(new PaymentConfigResponse(paymentService.getClientKey()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        final PaymentConfirmResponse response = paymentService.confirm(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/fail")
    public ResponseEntity<PaymentFailResponse> fail(
            @Valid @RequestBody PaymentFailRequest request
    ) {
        final PaymentFailResponse response = paymentService.fail(request);

        return ResponseEntity.ok(response);
    }
}
