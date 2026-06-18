package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/page")
public class PageController {

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/admin.html";
    }

    @GetMapping("/my-reservations")
    public String myReservationPage() {
        return "forward:/my-reservations.html";
    }

    @GetMapping("/reservations")
    public String reservationPage() {
        return "forward:/reservations.html";
    }

    @GetMapping("/payments/checkout")
    public String paymentCheckoutPage() {
        return "forward:/payments/checkout.html";
    }

    @GetMapping("/payments/success")
    public String paymentSuccessPage() {
        return "forward:/payments/success.html";
    }

    @GetMapping("/payments/fail")
    public String paymentFailPage() {
        return "forward:/payments/fail.html";
    }
}
