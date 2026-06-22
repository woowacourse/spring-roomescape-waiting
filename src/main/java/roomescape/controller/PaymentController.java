package roomescape.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.PaymentConfirmResponse;
import roomescape.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        PaymentConfirmResponse response = paymentService.confirm(paymentKey, orderId, amount);
        if (!response.isConfirmed()) {
            log.info("결제 결과 불명확 — 202 반환: orderId={}", orderId);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        if (orderId == null) {
            log.info("결제 취소(orderId 없음): code={}, message={}", code, message);
            return ResponseEntity.ok().build();
        }
        log.info("결제 실패: orderId={}, code={}, message={}", orderId, code, message);
        paymentService.cancel(orderId);
        return ResponseEntity.ok().build();
    }
}
