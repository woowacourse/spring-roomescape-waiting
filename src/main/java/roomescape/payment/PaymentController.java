package roomescape.payment;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.payment.dto.PaymentConfirmRequest;
import roomescape.payment.dto.PaymentFailRequest;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResult> confirm(
            @Valid @RequestBody PaymentConfirmRequest request,
            @LoginMember Long memberId
    ) {
        PaymentResult result = paymentService.confirm(
                memberId, request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestBody(required = false) PaymentFailRequest request,
            @LoginMember Long memberId
    ) {
        paymentService.fail(memberId, request == null ? null : request.orderId());
        return ResponseEntity.noContent().build();
    }
}
