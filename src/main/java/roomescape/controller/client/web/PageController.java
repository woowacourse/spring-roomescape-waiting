package roomescape.controller.client.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.service.PaymentService;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PaymentService paymentService;

    @GetMapping("/reserve")
    public String reserve() {
        return "forward:/reservation.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/search")
    public String search() {
        return "forward:/search.html";
    }

    @GetMapping("/payment/checkout")
    public String paymentCheckout(
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam String orderName,
            @RequestParam String clientKey,
            Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);
        model.addAttribute("clientKey", clientKey);
        return "payment/checkout";
    }

    @GetMapping("/payments/success")
    public String paymentsSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {
        try {
            var result = paymentService.confirm(paymentKey, orderId, amount);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            model.addAttribute("result", result);
            return "payment/success";
        } catch (Exception e) {
            model.addAttribute("code", "CONFIRM_FAILED");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("orderId", orderId);
            return "payment/fail";
        }
    }

    @GetMapping("/payments/fail")
    public String paymentsFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId,
            Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }
}
