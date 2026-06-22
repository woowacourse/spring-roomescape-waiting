package roomescape.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentResultUnknownException;
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
                    .location(URI.create(
                            "/payment-success.html?orderId=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8)))
                    .build();
        } catch (PaymentAmountMismatchException e) {
            return redirectToFail("AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            return redirectToFail(e.getCode(), e.getMessage(), orderId);
        } catch (PaymentConnectionException e) {
            return redirectToFail("CONNECTION_ERROR", "결제 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.", orderId);
        } catch (PaymentResultUnknownException e) {
            paymentService.markPaymentUnknown(orderId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(
                            "/payment-pending.html?orderId=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8)))
                    .build();
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
        String encodedCode = code != null ? URLEncoder.encode(code, StandardCharsets.UTF_8) : "";
        String encodedMessage = message != null ? URLEncoder.encode(message, StandardCharsets.UTF_8) : "";

        String uri = "/payment-fail.html?code=" + encodedCode
                + "&message=" + encodedMessage
                + (orderId != null ? "&orderId=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8) : "");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(uri))
                .build();
    }
}
