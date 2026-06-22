package roomescape.payment.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.payment.OrderNotFoundException;
import roomescape.payment.PaymentGatewayConnectionException;
import roomescape.payment.PaymentGatewayNoResponseException;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentService;
import roomescape.payment.client.OutboundRateLimitException;
import roomescape.payment.client.TossPaymentException;

/**
 * Toss 결제위젯 성공/실패 콜백을 처리한다.
 */
@Controller
public class CheckoutController {

    private final PaymentService paymentService;

    public CheckoutController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/success")
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
            return "payment/success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (OrderNotFoundException e) {
            return failView(model, "ORDER_NOT_FOUND", e.getMessage(), orderId);
        } catch (PaymentGatewayNoResponseException e) {
            return failView(model, "PAYMENT_CONFIRM_UNKNOWN", e.getMessage(), orderId);
        } catch (PaymentGatewayConnectionException e) {
            return failView(model, "PAYMENT_GATEWAY_UNAVAILABLE", e.getMessage(), orderId);
        } catch (OutboundRateLimitException e) {
            return failView(model, "OUTBOUND_RATE_LIMITED", e.getMessage(), orderId);
        } catch (TossPaymentException e) {
            return failView(model, e.getCode(), e.getMessage(), orderId);
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        paymentService.fail(code, message, orderId);
        return failView(model, code, message, orderId);
    }

    @PostMapping("/payments/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancel(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId
    ) {
        paymentService.fail(code, message, orderId);
        return ResponseEntity.noContent().build();
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
