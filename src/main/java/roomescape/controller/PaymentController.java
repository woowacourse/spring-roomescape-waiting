package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.client.PaymentConfirmationUnknownException;
import roomescape.client.PaymentException;
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;
import roomescape.service.PaymentService;

@Controller
public class PaymentController {

    private static final String ORDER_NAME = "방탈출 예약";

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(
            PaymentOrderRepository paymentOrderRepository,
            PaymentService paymentService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam long reservationId, Model model) {
        PaymentOrder order = paymentOrderRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", order.orderId());
        model.addAttribute("orderName", ORDER_NAME);
        model.addAttribute("amount", order.amount());
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
        } catch (PaymentConfirmationUnknownException e) {
            return failView(model, "결제 확인이 필요해요", e.getCode(), e.getMessage(), orderId);
        } catch (PaymentException e) {
            return failView(model, "결제를 실패했어요", e.getCode(), e.getMessage(), orderId);
        } catch (RuntimeException e) {
            return failView(model, "결제를 실패했어요", "PAYMENT_FAILED", e.getMessage(), orderId);
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
        return failView(model, "결제를 실패했어요", code, message, orderId);
    }

    private String failView(Model model, String title, String code, String message, String orderId) {
        model.addAttribute("title", title);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }
}
