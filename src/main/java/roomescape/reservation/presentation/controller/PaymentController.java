package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.PaymentConfirmCommand;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.service.PaymentCommandService;
import roomescape.reservation.application.service.PaymentQueryService;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.presentation.dto.PaymentConfigResponse;
import roomescape.reservation.presentation.dto.PaymentConfirmRequest;
import roomescape.reservation.presentation.dto.PaymentConfirmResponse;
import roomescape.reservation.presentation.dto.PaymentFailRequest;
import roomescape.reservation.presentation.dto.PaymentHistoryResponse;

@Validated
@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final ReservationCommandService reservationCommandService;
    private final String tossClientKey;

    public PaymentController(
            PaymentCommandService paymentCommandService,
            PaymentQueryService paymentQueryService,
            ReservationCommandService reservationCommandService,
            @Value("${toss.client-key}") String tossClientKey
    ) {
        this.paymentCommandService = paymentCommandService;
        this.paymentQueryService = paymentQueryService;
        this.reservationCommandService = reservationCommandService;
        this.tossClientKey = tossClientKey;
    }

    @GetMapping
    public ResponseEntity<List<PaymentHistoryResponse>> findByName(
            @NotBlank(message = "이름은 비어있을 수 없습니다.")
            @RequestParam
            String username
    ) {
        List<PaymentHistoryResponse> responses = paymentQueryService.findByName(username).stream()
                .map(PaymentHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
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
        PaymentResult result = paymentCommandService.confirm(command);
        return ResponseEntity.ok(PaymentConfirmResponse.of(result));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> paymentFail(
            @Valid @RequestBody PaymentFailRequest request
    ) {
        reservationCommandService.cleanupPendingPaymentFailure(request.toCommand(LocalDateTime.now()));
        return ResponseEntity.noContent().build();
    }
}
