package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.PaymentConfirmCommand;
import roomescape.reservation.application.service.PaymentService;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.presentation.dto.PaymentConfigResponse;
import roomescape.reservation.presentation.dto.PaymentConfirmRequest;
import roomescape.reservation.presentation.dto.PaymentConfirmResponse;
import roomescape.reservation.presentation.dto.PaymentFailRequest;

@Validated
@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationCommandService reservationCommandService;
    private final String tossClientKey;

    public PaymentController(
            PaymentService paymentService,
            ReservationCommandService reservationCommandService,
            @Value("${toss.client-key}") String tossClientKey
    ) {
        this.paymentService = paymentService;
        this.reservationCommandService = reservationCommandService;
        this.tossClientKey = tossClientKey;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> paymentConfig() {
        return ResponseEntity.ok(new PaymentConfigResponse(tossClientKey));
    }

    @PostMapping("/success")
    public ResponseEntity<PaymentConfirmResponse> paymentSuccess(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmCommand command = request.toCommand();
        var result = paymentService.confirm(command);
        return ResponseEntity.ok(PaymentConfirmResponse.of(
                result.paymentKey(),
                result.orderId(),
                result.status().name(),
                result.approvedAmount()
        ));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> paymentFail(
            @Valid @RequestBody PaymentFailRequest request
    ) {
        reservationCommandService.cleanupPendingPaymentFailure(request.toCommand(LocalDateTime.now()));
        return ResponseEntity.noContent().build();
    }
}
