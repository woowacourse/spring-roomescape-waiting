package roomescape.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.payment.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey, @RequestParam String orderId, @RequestParam Long amount,
                          Model model) {
        paymentService.confirm(paymentKey, orderId, amount);
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentKey", paymentKey);
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
