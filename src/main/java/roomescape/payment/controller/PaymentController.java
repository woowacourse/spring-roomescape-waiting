package roomescape.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.PaymentResult;
import roomescape.payment.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResult> confirm(@RequestBody PaymentConfirmRequest request) {
        PaymentResult result = paymentService.confirm(
                request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryResponse>> history(@RequestParam String name) {
        return ResponseEntity.ok(paymentService.findHistoryByName(name));
    }
}
