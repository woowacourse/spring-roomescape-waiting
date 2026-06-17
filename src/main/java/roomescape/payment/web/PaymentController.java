package roomescape.payment.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.payment.PaymentService;
import roomescape.payment.web.PaymentConfirmRequestDto;
import roomescape.payment.web.PaymentFailRequestDto;
import roomescape.payment.web.PaymentClientConfigResponse;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentClientConfigResponse> config() {
        return ResponseEntity.ok(new PaymentClientConfigResponse(paymentService.clientKey()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@LoginMember Member member,
                                        @Valid @RequestBody PaymentConfirmRequestDto request) {
        paymentService.confirm(member, request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(@LoginMember Member member,
                                     @RequestBody PaymentFailRequestDto request) {
        paymentService.fail(member, request.orderId());
        return ResponseEntity.ok().build();
    }
}
