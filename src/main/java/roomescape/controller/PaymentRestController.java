package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.payment.PaymentConfirmRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.PaymentService;

@RestController
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<ReservationResponse> confirm(@RequestBody PaymentConfirmRequest paymentConfirmRequest) {
        ReservationResponse reservationResponse = paymentService.confirm(paymentConfirmRequest);
        return ResponseEntity.ok(reservationResponse);
    }
}
