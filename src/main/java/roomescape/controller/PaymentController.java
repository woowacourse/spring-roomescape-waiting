package roomescape.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
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

    @GetMapping("/fail")
    public ResponseEntity<Void> failPayment(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.failPayment(orderId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(failPageUri(code, message, orderId))
                .build();
    }

    private URI failPageUri(String code, String message, String orderId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/payment-fail.html")
                .queryParam("code", code)
                .queryParam("message", message);
        if (orderId != null && !orderId.isBlank()) {
            builder.queryParam("orderId", orderId);
        }
        return builder.build()
                .encode()
                .toUri();
    }
}
