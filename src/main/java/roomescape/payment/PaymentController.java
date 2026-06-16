package roomescape.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.payment.service.PaymentService;
import roomescape.payment.service.dto.PaymentConfirmationRequest;
import roomescape.payment.service.dto.PaymentResult;

import static roomescape.member.domain.Role.MANAGER;
import static roomescape.member.domain.Role.MEMBER;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PostMapping("/payments/confirm")
    public ResponseEntity<PaymentResult> confirm(@RequestBody PaymentConfirmationRequest request) {
        PaymentResult responseData = paymentService.confirm(request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(responseData);
    }

}
