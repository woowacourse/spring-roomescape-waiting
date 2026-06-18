package roomescape.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.client.PaymentConnectionException;
import roomescape.payment.client.PaymentTimeoutException;
import roomescape.payment.client.TossPaymentException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentAmountMismatchException;
import roomescape.payment.repository.PaymentRepository;
import roomescape.payment.service.PaymentService;

@Controller
public class CheckoutController {

    private static final String ORDER_NAME = "방탈출 예약";

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final String clientKey;

    public CheckoutController(
            PaymentService paymentService,
            PaymentRepository paymentRepository,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/checkout")
    public String checkout(@RequestParam String orderId, Model model) {
        Payment payment = paymentRepository.getByOrderId(orderId);

        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", payment.getOrderId());
        model.addAttribute("orderName", ORDER_NAME);
        model.addAttribute("amount", payment.getAmount());
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
            paymentService.confirm(paymentKey, orderId, amount);
            return "redirect:/my-reservations";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
        } catch (PaymentTimeoutException e) {
            model.addAttribute("paymentKey", paymentKey);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            model.addAttribute("message", e.getMessage());
            return "payment-uncertain";
        } catch (PaymentConnectionException e) {
            return failView(model, "GATEWAY_CONNECTION", e.getMessage(), orderId);
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
        paymentService.cancelPending(orderId);
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}