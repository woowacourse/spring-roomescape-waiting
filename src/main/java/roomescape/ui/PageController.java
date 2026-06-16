package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String userPage() {
        return "user";
    }

    @GetMapping("/reservation-lookup")
    public String reservationLookupPage() {
        return "reservation-lookup";
    }

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @GetMapping("/admin-page")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/payment/success")
    public String paymentSuccessPage() {
        return "payment-success";
    }

    @GetMapping("/payment/fail")
    public String paymentFailPage() {
        return "payment-fail";
    }
}
