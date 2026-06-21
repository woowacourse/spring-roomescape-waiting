package roomescape.payment.presentation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.application.service.PaymentService;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.presentation.dto.PaymentOrderHistoryResponse;
import roomescape.payment.presentation.dto.PaymentOrderRequest;
import roomescape.payment.presentation.dto.PaymentOrderResponse;

@RequiredArgsConstructor
@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @Valid @RequestBody PaymentOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentOrderResponse.from(
                        paymentService.createOrder(request.toCommand(), LocalDateTime.now())));
    }

    @GetMapping("/success")
    public ResponseEntity<Void> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        return confirmAndRedirect(new PaymentConfirmation(paymentKey, orderId, amount));
    }

    @GetMapping("/retry")
    public ResponseEntity<Void> retry(@RequestParam String orderId) {
        try {
            paymentService.retry(orderId);
            return redirect("success", "결제가 승인되어 예약이 확정되었습니다.");
        } catch (PaymentException exception) {
            return handleConfirmationFailure(orderId, null, exception);
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<PaymentOrderHistoryResponse>> findOrders(@RequestParam String name) {
        return ResponseEntity.ok(paymentService.findAllByName(name).stream()
                .map(PaymentOrderHistoryResponse::from)
                .toList());
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.fail(code, orderId);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/?payment=fail&message=" + encodedMessage + "#/reserve"))
                .build();
    }

    private ResponseEntity<Void> confirmAndRedirect(PaymentConfirmation confirmation) {
        try {
            paymentService.confirm(confirmation);
            return redirect("success", "결제가 승인되어 예약이 확정되었습니다.");
        } catch (PaymentException exception) {
            return handleConfirmationFailure(
                    confirmation.orderId(),
                    confirmation.paymentKey(),
                    exception
            );
        }
    }

    private ResponseEntity<Void> handleConfirmationFailure(
            String orderId,
            String paymentKey,
        PaymentException exception
    ) {
        if (exception.errorCode() == PaymentErrorCode.CONFIRMATION_UNKNOWN) {
            paymentService.markConfirmationUnknown(orderId, paymentKey);
            return redirect("unknown", exception.getMessage());
        }
        if (exception.errorCode() == PaymentErrorCode.GATEWAY_CONNECTION_FAILED) {
            paymentService.keepPending(orderId, paymentKey);
            return redirect("retry", exception.getMessage());
        }
        paymentService.markFailed(orderId);
        return redirect("fail", exception.getMessage());
    }

    private ResponseEntity<Void> redirect(String payment, String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(
                        "/?payment=" + payment + "&message=" + encodedMessage + "#/reservations"
                ))
                .build();
    }
}
