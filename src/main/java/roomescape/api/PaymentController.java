package roomescape.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationApplicationService;
import roomescape.domain.Reservation;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.PaymentUncertainException;
import roomescape.payment.toss.TossPaymentException;
import roomescape.service.PaymentService;

@RestController
public class PaymentController {

    private final ReservationApplicationService reservationApplicationService;
    private final PaymentService paymentService;

    public PaymentController(ReservationApplicationService reservationApplicationService,
                             PaymentService paymentService) {
        this.reservationApplicationService = reservationApplicationService;
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/{orderId}/confirmation")
    public ResponseEntity<ReservationResponse> confirm(
            @PathVariable String orderId,
            @RequestBody @Valid PaymentConfirmRequest request
    ) {
        try {
            Reservation confirmed = reservationApplicationService.confirmReservation(orderId, request);
            return ResponseEntity.ok(ReservationResponse.from(confirmed));
        } catch (PaymentUncertainException e) {
            paymentService.markUncertain(orderId);
            throw e;
        } catch (TossPaymentException e) {
            paymentService.markFailed(orderId);
            throw e;
        }
    }

    @DeleteMapping("/payments/{orderId}")
    public ResponseEntity<Void> deletePendingPayment(@PathVariable String orderId) {
        reservationApplicationService.deletePendingPayment(orderId);

        return ResponseEntity.noContent().build();
    }
}
