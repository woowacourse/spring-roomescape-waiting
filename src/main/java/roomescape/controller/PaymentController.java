package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.PaymentFailResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.payment.PaymentFailure;
import roomescape.service.ReservationPaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final ReservationPaymentService reservationPaymentService;

    public PaymentController(ReservationPaymentService reservationPaymentService) {
        this.reservationPaymentService = reservationPaymentService;
    }

    @GetMapping("/success")
    public ResponseEntity<ReservationResponse> confirmPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        Reservation reservation = reservationPaymentService.confirm(paymentKey, orderId, amount);
        return ResponseEntity.ok(ReservationResponse.fromReserved(reservation, reservation.getTheme()));
    }

    @GetMapping("/fail")
    public ResponseEntity<PaymentFailResponse> failPayment(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        PaymentFailure failure = reservationPaymentService.fail(code, message, orderId);
        return ResponseEntity.ok(PaymentFailResponse.from(failure));
    }
}
