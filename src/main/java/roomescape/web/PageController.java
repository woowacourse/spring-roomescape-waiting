package roomescape.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    @GetMapping("/my-reservations")
    public String myReservations() {
        return "my-reservations";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess() {
        return "payment-success";
    }

    @GetMapping("/payment/fail")
    public String paymentFail() {
        return "payment-fail";
    }

    @GetMapping("/payment/checkout")
    public String paymentCheckout() {
        return "payment-checkout";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
