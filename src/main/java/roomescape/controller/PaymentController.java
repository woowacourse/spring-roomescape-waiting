package roomescape.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.ReservationPaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final ReservationPaymentService reservationPaymentService;

    public PaymentController(ReservationPaymentService reservationPaymentService) {
        this.reservationPaymentService = reservationPaymentService;
    }

    @GetMapping("/success")
    public ResponseEntity<Void> confirmPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        reservationPaymentService.confirm(paymentKey, orderId, amount);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/user.html?payment=success#my"))
                .build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> failPayment(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        reservationPaymentService.fail(code, message, orderId);
        URI location = UriComponentsBuilder.fromPath("/user.html")
                .queryParam("payment", "fail")
                .queryParam("code", code)
                .queryParam("message", message)
                .fragment("booking")
                .build()
                .encode()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }
}
