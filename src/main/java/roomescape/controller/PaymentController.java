package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.toss.TossPaymentException;
import roomescape.service.PaymentService;

import java.net.URI;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/payment-success.html?orderId=" + orderId))
                    .build();
        } catch (PaymentAmountMismatchException e) {
            return redirectToFail("AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            return redirectToFail(e.getCode(), e.getMessage(), orderId);
        }
    }

    @GetMapping("/payments/fail")
    public ResponseEntity<Void> fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId
    ) {
        if (orderId != null) {
            paymentService.cancelPendingReservation(orderId);
        }
        return redirectToFail(code, message, orderId);
    }

    private ResponseEntity<Void> redirectToFail(String code, String message, String orderId) {
        String uri = "/payment-fail.html?code=" + (code != null ? code : "")
                + "&message=" + (message != null ? message : "")
                + (orderId != null ? "&orderId=" + orderId : "");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(uri))
                .build();
    }
}
