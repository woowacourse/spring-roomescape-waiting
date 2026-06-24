package roomescape.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.config.TossPaymentProperties;
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
    public String success(@RequestParam String paymentKey, @RequestParam String orderId, @RequestParam Long amount) {
        paymentService.confirm(paymentKey, orderId, amount);
        return "success";
    }

    @GetMapping("/fail")
    public String fail(@RequestParam(required = false) String code, @RequestParam(required = false) String message,
                       @RequestParam(required = false) String orderId, Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";  // fail.html 필요
    }
}
