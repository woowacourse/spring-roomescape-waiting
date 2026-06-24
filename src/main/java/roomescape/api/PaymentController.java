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

@RestController
public class PaymentController {

    private final ReservationApplicationService reservationApplicationService;

    public PaymentController(ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @PostMapping("/payments/{orderId}/confirmation")
    public ResponseEntity<ReservationResponse> confirm(
            @PathVariable String orderId,
            @RequestBody @Valid PaymentConfirmRequest request
    ) {
        Reservation confirmed = reservationApplicationService.confirmReservation(orderId, request);

        return ResponseEntity.ok(ReservationResponse.from(confirmed));
    }

    @DeleteMapping("/payments/{orderId}")
    public ResponseEntity<Void> deletePendingPayment(@PathVariable String orderId) {
        reservationApplicationService.deletePendingPayment(orderId);

        return ResponseEntity.noContent().build();
    }
}
