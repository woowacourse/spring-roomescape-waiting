package roomescape.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.TossPaymentException;
import roomescape.payment.service.PaymentService;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            var result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            model.addAttribute("paymentKey", paymentKey);
            return "payment-success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId, false);
        } catch (TossPaymentException e) {
            return failView(model, e.getCode(), e.getMessage(), orderId, e.isRetryable());
        } catch (Exception e) {
            return failView(model, "UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다.", orderId, true);
        }
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        paymentService.cancelPendingByOrderId(orderId);
        return failView(model, code, message, orderId, false);
    }

    private String failView(Model model, String code, String message, String orderId, boolean retryable) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        model.addAttribute("retryable", retryable);
        return "payment-fail";
    }
}
