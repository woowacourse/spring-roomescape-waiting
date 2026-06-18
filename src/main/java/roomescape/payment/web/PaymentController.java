package roomescape.payment.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.payment.ConfirmOutcome;
import roomescape.payment.OrderAbandonmentService;
import roomescape.payment.PaymentHistoryService;
import roomescape.payment.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentHistoryService paymentHistoryService;
    private final OrderAbandonmentService abandonmentService;

    public PaymentController(PaymentService paymentService, PaymentHistoryService paymentHistoryService,
                            OrderAbandonmentService abandonmentService) {
        this.paymentService = paymentService;
        this.paymentHistoryService = paymentHistoryService;
        this.abandonmentService = abandonmentService;
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentClientConfigResponse> config() {
        return ResponseEntity.ok(new PaymentClientConfigResponse(paymentService.clientKey()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyOrderResponse>> myOrders(@LoginMember Member member) {
        return ResponseEntity.ok(paymentHistoryService.findMyOrders(member.getId()));
    }

    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> ready(@LoginMember Member member,
                                                      @Valid @RequestBody PaymentReadyRequestDto request) {
        return ResponseEntity.ok(paymentService.prepare(member, request.reservationId()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(@LoginMember Member member,
                                                          @Valid @RequestBody PaymentConfirmRequestDto request) {
        ConfirmOutcome outcome = paymentService.confirm(
                member, request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(new PaymentConfirmResponse(outcome));
    }

    @PostMapping("/recheck")
    public ResponseEntity<PaymentConfirmResponse> recheck(@LoginMember Member member,
                                                          @RequestBody PaymentRecheckRequestDto request) {
        ConfirmOutcome outcome = paymentService.recheck(member, request.orderId());
        return ResponseEntity.ok(new PaymentConfirmResponse(outcome));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(@LoginMember Member member,
                                     @RequestBody PaymentFailRequestDto request) {
        abandonmentService.fail(member, request.orderId());
        return ResponseEntity.ok().build();
    }
}
