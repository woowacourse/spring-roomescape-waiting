package roomescape.adapter.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.application.PaymentService;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.server.PaymentTimeoutException;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 인증 성공 → 서버 승인(요구사항 3·4). 승인 실패 시 결과 페이지로 사유 전달.
     */
    @GetMapping("/success")
    public String success(@RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam long amount) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
            return "redirect:/payment-result.html?status=success";
        } catch (PaymentTimeoutException e) {                 // 확인 필요 — 삭제 금지
            paymentService.markInDoubt(orderId);
            return "redirect:/payment-result.html?status=pending&message=" + encode(e.getMessage());
        } catch (RoomEscapeException e) {                      // 거절·연결실패 등 — 안전하게 정리
            paymentService.cancelPending(orderId);
            return "redirect:/payment-result.html?status=fail&message=" + encode(e.getMessage());
        }
    }

    /**
     * 실패/취소 콜백(요구사항 7). 취소(PAY_PROCESS_CANCELED)면 orderId가 없을 수 있어 null 가드.
     */
    @GetMapping("/fail")
    public String fail(@RequestParam(required = false) String orderId,
                       @RequestParam(required = false) String code,
                       @RequestParam(required = false) String message) {
        if (orderId != null) {
            paymentService.cancelPending(orderId);
        }
        String reason = (message != null) ? message : "결제가 취소되었습니다.";
        return "redirect:/payment-result.html?status=fail&message=" + encode(reason);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
