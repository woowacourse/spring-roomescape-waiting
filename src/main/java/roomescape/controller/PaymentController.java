package roomescape.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.PaymentConfirmResponse;
import roomescape.controller.dto.PaymentFailResponse;
import roomescape.controller.dto.ReservationPaymentResponse;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.service.PaymentService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        Payment payment = paymentService.confirmPayment(confirmation);
        return ResponseEntity.ok(PaymentConfirmResponse.from(payment));
    }

    @GetMapping("/fail")
    public ResponseEntity<PaymentFailResponse> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.handlePaymentFail(orderId);
        return ResponseEntity.ok(new PaymentFailResponse(code, message));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReservationPaymentResponse>> getHistory(@NotBlank @RequestParam String name) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(name));
    }
}
