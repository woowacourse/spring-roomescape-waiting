package roomescape.domain.payment;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping({"/payments/confirm", "/confirm/payment"})
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
        @RequestBody @Valid PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentService.confirm(request);
        return ResponseEntity.ok(response);
    }
}
