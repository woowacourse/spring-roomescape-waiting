package roomescape.payment.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.application.service.PaymentService;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.presentation.dto.PaymentOrderRequest;
import roomescape.payment.presentation.dto.PaymentOrderResponse;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @Valid @RequestBody PaymentOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentOrderResponse.from(
                        paymentService.createOrder(request.toCommand(), LocalDateTime.now())));
    }

    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        paymentService.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/?payment=success#/reservations"))
                .build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.fail(code, orderId);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/?payment=fail&message=" + encodedMessage + "#/reserve"))
                .build();
    }
}
