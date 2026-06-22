package roomescape.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.payment.PaymentConfirmUnknownException;
import roomescape.payment.PaymentConnectionException;
import roomescape.service.ReservationPaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final ReservationPaymentService reservationPaymentService;

    public PaymentController(ReservationPaymentService reservationPaymentService) {
        this.reservationPaymentService = reservationPaymentService;
    }

    @GetMapping("/success")
    public ResponseEntity<Void> confirmPayment(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam long amount
    ) {
        try {
            reservationPaymentService.confirm(paymentKey, orderId, amount);
        } catch (PaymentConfirmUnknownException e) {
            return redirectToUser("unknown", "결제 승인 결과를 확인하고 있습니다. 내 예약에서 결제 상태를 확인해 주세요.", "my");
        } catch (PaymentConnectionException e) {
            return redirectToUser("connection-fail", "토스 결제 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.", "my");
        }
        return redirect(URI.create("/user.html?payment=success#my"));
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> failPayment(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        reservationPaymentService.fail(code, message, orderId);
        return redirect(failLocation(code, message));
    }

    private URI failLocation(String code, String message) {
        return UriComponentsBuilder.fromPath("/user.html")
                .queryParam("payment", "fail")
                .queryParam("code", code)
                .queryParam("message", message)
                .fragment("booking")
                .build()
                .encode()
                .toUri();
    }

    private ResponseEntity<Void> redirectToUser(String payment, String message, String fragment) {
        return redirect(userLocation(payment, message, fragment));
    }

    private URI userLocation(String payment, String message, String fragment) {
        return UriComponentsBuilder.fromPath("/user.html")
                .queryParam("payment", payment)
                .queryParam("message", message)
                .fragment(fragment)
                .build()
                .encode()
                .toUri();
    }

    private ResponseEntity<Void> redirect(URI location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }
}
