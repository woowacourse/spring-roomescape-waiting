package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.payment.Order;
import roomescape.dto.payment.PaymentPrepareRequest;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.service.PaymentService;
import roomescape.service.ReservationService;

import java.net.URI;

@RestController
public class PaymentRestController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    public PaymentRestController(PaymentService paymentService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @PostMapping("/payments/prepare")
    public ResponseEntity<Void> prepare(@RequestBody PaymentPrepareRequest request) {
        paymentService.prepare(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount) {
        paymentService.confirm(paymentKey, orderId, amount);
        Order order = paymentService.getOrder(orderId);
        reservationService.create(new ReservationRequest(order.getName(), order.getDate(), order.getTimeId(), order.getThemeId()));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/payment-success.html"))
                .build();
    }

    @GetMapping("/payment/fail")
    public ResponseEntity<Void> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/payment-fail.html?code=" + code + "&message=" + message))
                .build();
    }
}