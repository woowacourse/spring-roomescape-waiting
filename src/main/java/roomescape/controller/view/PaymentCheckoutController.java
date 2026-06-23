package roomescape.controller.view;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import roomescape.domain.Payment;
import roomescape.service.PaymentService;

@Controller
public class PaymentCheckoutController {

    private static final String ORDER_NAME = "방탈출 예약";

    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentCheckoutController(
            PaymentService paymentService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/{paymentId}/checkout")
    public String checkout(@PathVariable Long paymentId, Model model) {
        Payment payment = paymentService.getReadyPayment(paymentId);
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("paymentId", payment.getId());
        model.addAttribute("orderId", payment.getOrderId());
        model.addAttribute("orderName", ORDER_NAME);
        model.addAttribute("amount", payment.getAmount());
        return "checkout";
    }
}
