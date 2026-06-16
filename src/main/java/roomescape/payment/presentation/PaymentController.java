package roomescape.payment.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.payment.application.OrderService;
import roomescape.payment.application.PaymentService;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.application.dto.PaymentCancelCommand;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.application.exception.PaymentAmountMismatchException;
import roomescape.payment.infra.client.exception.TossPaymentException;
import roomescape.payment.presentation.dto.PaymentCancelRequest;
import roomescape.payment.presentation.dto.PaymentCancelResponse;
import roomescape.payment.presentation.dto.PaymentRequest;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @GetMapping("/success")
    public String confirm(@ModelAttribute PaymentRequest request, Model model) {
        try {
            PaymentResult result = paymentService.confirm(request.paymentKey(), request.orderId(), request.amount());
            OrderInfo order = orderService.getOrder(request.orderId());
            model.addAttribute("result", result);
            model.addAttribute("paymentKey", result.paymentKey());
            model.addAttribute("username", order.username());
            return "success";
        } catch (PaymentAmountMismatchException e) {
            paymentService.fail(request.orderId());
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), request.orderId());
        } catch (TossPaymentException e) {
            paymentService.fail(request.orderId());
            return failView(model, e.getCode(), e.getMessage(), request.orderId());
        }
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        paymentService.fail(orderId);
        return failView(model, code, message, orderId);
    }

    @ResponseBody
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<PaymentCancelResponse> cancel(@PathVariable String orderId, @RequestBody PaymentCancelRequest request) {
        PaymentResult cancelled = paymentService.cancel(orderId, PaymentCancelCommand.toCommand(request));
        return ResponseEntity.ok(PaymentCancelResponse.from(orderId, cancelled));
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}
