package roomescape.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.dto.request.PaymentConfirmRequest;
import roomescape.payment.PaymentResult;
import roomescape.service.PaymentService;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${toss.client-key}")
    private String clientKey;

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam String orderId,
            @RequestParam Long amount,
            @RequestParam String orderName,
            Model model
    ) {
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("orderName", orderName);
        return "checkout";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @PostMapping("/confirm")
    @ResponseBody
    public ResponseEntity<PaymentResult> confirm(@RequestBody PaymentConfirmRequest request) {
        PaymentResult result = paymentService.confirm(request.paymentKey(), request.orderId(), request.amount());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fail")
    public String fail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "fail";
    }

}
