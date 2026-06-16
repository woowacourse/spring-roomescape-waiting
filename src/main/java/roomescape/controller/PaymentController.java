package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import roomescape.controller.dto.payment.PaymentConfirmResponse;
import roomescape.controller.dto.payment.PaymentFailResponse;
import roomescape.controller.dto.payment.PaymentOrderRequest;
import roomescape.controller.dto.payment.PaymentOrderResponse;
import roomescape.domain.Member;
import roomescape.global.auth.LoginMember;
import roomescape.service.PaymentService;

@RequestMapping("/payments")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @Valid @RequestBody PaymentOrderRequest request,
            @LoginMember Member member,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(paymentService.createOrder(request, member, baseUrl(servletRequest)));
    }

    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount
    ) {
        PaymentConfirmResponse response = paymentService.confirm(paymentKey, orderId, amount);
        String body = """
                <!doctype html>
                <html lang="ko">
                <head><meta charset="UTF-8"><title>결제 완료</title></head>
                <body>
                <script>
                  alert('예약이 확정되었습니다.');
                  location.href = '/user.html';
                </script>
                <p>예약이 확정되었습니다. 예약 ID: %d</p>
                <p><a href="/user.html">내 예약으로 이동</a></p>
                </body>
                </html>
                """.formatted(response.reservationId());
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/fail", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId
    ) {
        PaymentFailResponse response = paymentService.fail(code, message, orderId);
        String body = """
                <!doctype html>
                <html lang="ko">
                <head><meta charset="UTF-8"><title>결제 실패</title></head>
                <body>
                <script>
                  alert('%s');
                  location.href = '/user.html';
                </script>
                <p>결제에 실패했습니다. %s</p>
                <p><a href="/user.html">예약 화면으로 이동</a></p>
                </body>
                </html>
                """.formatted(escape(response.message()), escape(response.message()));
        return ResponseEntity.ok(body);
    }

    private String baseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
