package roomescape.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import roomescape.common.exception.BusinessException;
import roomescape.config.TossPaymentProperties;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentUncertainException;
import roomescape.payment.TossPaymentException;
import roomescape.payment.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final TossPaymentProperties tossPaymentProperties;

    public PaymentController(PaymentService paymentService, TossPaymentProperties tossPaymentProperties) {
        this.paymentService = paymentService;
        this.tossPaymentProperties = tossPaymentProperties;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam String orderId, @RequestParam String orderName, Model model) {
        // amount 는 클라이언트가 보낸 값을 믿지 않고 결제 대기 시점에 저장한 주문 금액을 DB 에서 다시 읽는다.
        model.addAttribute("clientKey", tossPaymentProperties.clientKey());
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", paymentService.getAmount(orderId));
        return "checkout";
    }

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey, @RequestParam String orderId, @RequestParam Long amount,
                          RedirectAttributes redirectAttributes) {
        try {
            paymentService.confirm(paymentKey, orderId, amount);
            return "success";
        } catch (TossPaymentException e) {
            // 승인 실패도 JSON(@RestControllerAdvice)이 아니라 사용자용 fail 페이지로 보낸다.
            return redirectToFail(redirectAttributes, e.getCode(), e.getMessage(), orderId);
        } catch (BusinessException e) {
            return redirectToFail(redirectAttributes, e.getErrorCode().name(), e.getMessage(), orderId);
        } catch (PaymentConnectionException e) {
            // Toss에 요청이 도달하지 않아 결제 안 됨 → fail 엔드포인트에서 예약 정리.
            return redirectToFail(redirectAttributes, "CONNECTION_FAILED", e.getMessage(), orderId);
        } catch (PaymentUncertainException e) {
            // read timeout: 결제 처리 여부 불명 → 예약을 PAYMENT_UNCERTAIN으로 보존, 삭제하지 않음.
            paymentService.markAsUncertain(orderId);
            return redirectToFail(redirectAttributes, "PAYMENT_UNCERTAIN", e.getMessage(), orderId);
        }
    }

    @GetMapping("/fail")
    public String fail(@RequestParam(required = false) String code, @RequestParam(required = false) String message,
                       @RequestParam(required = false) String orderId, Model model) {
        // 사용자가 결제창에서 취소(PAY_PROCESS_CANCELED)하면 orderId 가 없을 수 있다 → null 가드.
        if (orderId != null) {
            paymentService.cancelPending(orderId);
        }
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "fail";
    }

    private String redirectToFail(RedirectAttributes redirectAttributes, String code, String message, String orderId) {
        redirectAttributes.addAttribute("code", code);
        redirectAttributes.addAttribute("message", message);
        redirectAttributes.addAttribute("orderId", orderId);
        return "redirect:/payments/fail";
    }
}
