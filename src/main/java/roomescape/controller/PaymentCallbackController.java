package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.client.PaymentGatewayException;
import roomescape.client.TossPaymentException;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.service.PaymentService;

@Controller
public class PaymentCallbackController {
    private final PaymentService paymentService;

    public PaymentCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            RedirectAttributes redirectAttributes
    ) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
            addRedirectAttributes(redirectAttributes, "success", "DONE", "결제가 완료되었습니다.", orderId, amount);
        } catch (RoomEscapeException exception) {
            addRedirectAttributes(redirectAttributes, "fail", paymentCode(exception), exception.getErrorCode().getMessage(), orderId, amount);
        } catch (TossPaymentException exception) {
            addRedirectAttributes(redirectAttributes, "fail", exception.getCode(), exception.getMessage(), orderId, amount);
        } catch (PaymentGatewayException.ReadTimeout exception) {
            addRedirectAttributes(redirectAttributes, "unknown", exception.getCode(), exception.getMessage(), orderId, amount);
        } catch (PaymentGatewayException exception) {
            addRedirectAttributes(redirectAttributes, "fail", exception.getCode(), exception.getMessage(), orderId, amount);
        }

        return "redirect:/reservation.html";
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            RedirectAttributes redirectAttributes
    ) {
        paymentService.fail(orderId);
        addRedirectAttributes(redirectAttributes, "fail", code, message, orderId, null);
        return "redirect:/reservation.html";
    }

    private void addRedirectAttributes(
            RedirectAttributes redirectAttributes,
            String payment,
            String code,
            String message,
            String orderId,
            Long amount
    ) {
        redirectAttributes.addAttribute("payment", payment);
        redirectAttributes.addAttribute("code", defaultValue(code, "PAYMENT_FAILED"));
        redirectAttributes.addAttribute("message", defaultValue(message, "결제 처리에 실패했습니다."));

        if (orderId != null && !orderId.isBlank()) {
            redirectAttributes.addAttribute("orderId", orderId);
        }
        if (amount != null) {
            redirectAttributes.addAttribute("amount", amount);
        }
    }

    private String paymentCode(RoomEscapeException exception) {
        if (exception.getErrorCode() instanceof PaymentErrorCode paymentErrorCode) {
            return paymentErrorCode.name();
        }
        return "PAYMENT_FAILED";
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
