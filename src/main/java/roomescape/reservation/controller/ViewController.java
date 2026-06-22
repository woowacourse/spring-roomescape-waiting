package roomescape.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.payment.infrastructure.TossProperties;

@Controller
public class ViewController {

    private final TossProperties tossProperties;

    public ViewController(TossProperties tossProperties) {
        this.tossProperties = tossProperties;
    }

    @GetMapping("/reservations")
    public String reservationPage(Model model) {
        model.addAttribute("clientKey", tossProperties.clientKey());
        return "reservation";
    }

    @GetMapping("/payments/success")
    public String paymentSuccessPage() {
        return "payment-success";
    }

    @GetMapping("/payments/fail")
    public String paymentFailPage() {
        return "payment-fail";
    }
}
