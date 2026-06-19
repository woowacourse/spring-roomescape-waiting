package roomescape.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.Payment;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.infrastructure.payment.toss.toss.TossPaymentException;
import roomescape.payment.service.PaymentService;

/**
 * 브라우저에서 Toss 결제위젯으로 결제 흐름 전체(인증 → 승인)를 체험하는 SSR(Thymeleaf) 컨트롤러. 미구현(initial) 상태에서는 위젯 인증까지만 되고 승인 결과가 채워지지 않는다.
 */
@Controller
public class CheckoutController {

    private static final String ORDER_NAME = "방탈출 예약 — 우아한 비밀의 방";

    private final PaymentService paymentService;
    private final String clientKey;

    public CheckoutController(
            PaymentService paymentService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/checkout")
    public String checkout(@RequestParam Long reservationId, Model model) {
        Payment payment = paymentService.findLatestOrderByReservationId(reservationId);

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
            var result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("result", result);
            model.addAttribute("paymentKey", paymentKey);
            return "success";
        } catch (PaymentAmountMismatchException e) {
            return failView(model, "AMOUNT_MISMATCH", e.getMessage(), orderId);
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
        // 사용자 취소 시 orderId 가 없을 수 있다.
        return failView(model, code, message, orderId);
    }

    private String failView(Model model, String code, String message, String orderId) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }

}
