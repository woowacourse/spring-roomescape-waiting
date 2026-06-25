package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.PreparePaymentRequest;
import roomescape.service.SessionService;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final SessionService sessionService;

    public PaymentController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/prepare")
    public ResponseEntity<Map<String, String>> prepare(@RequestBody @Valid PreparePaymentRequest request) {
        String orderId = sessionService.preparePayment(request.amount());
        return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    @DeleteMapping("/prepare/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable String orderId) {
        sessionService.cancelPreparedPayment(orderId);
        return ResponseEntity.noContent().build();
    }
}
