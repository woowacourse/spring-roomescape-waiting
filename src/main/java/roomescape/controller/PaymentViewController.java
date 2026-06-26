package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.NotFoundException;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.PaymentConfirmationUnknownException;
import roomescape.exception.PaymentConnectionException;
import roomescape.exception.PaymentGatewayException;
import roomescape.infra.toss.TossPaymentException;
import roomescape.service.PaymentService;

@Controller
public class PaymentViewController {

    private static final String DEFAULT_ORDER_NAME = "방탈출 예약";

    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentViewController(
            PaymentService paymentService,
            @Value("${toss.client-key}") String clientKey
    ) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam(defaultValue = DEFAULT_ORDER_NAME) String orderName,
            Model model
    ) {
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        return "checkout";
    }

    @GetMapping("/payments/success")
    public String success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model
    ) {
        try {
            PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            return "success";
        } catch (PaymentAmountMismatchException | NotFoundException exception) {
            return failView(model, exception.getErrorCode(), exception.getMessage(), orderId);
        } catch (PaymentConnectionException | PaymentConfirmationUnknownException | PaymentGatewayException exception) {
            return failView(model, exception.getErrorCode(), exception.getMessage(), orderId);
        } catch (TossPaymentException exception) {
            return failView(model, exception.getCode(), exception.getMessage(), orderId);
        }
    }

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}
