package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/reservations")
    public String userReservationPage() {
        return "user/reservations";
    }

    @GetMapping("/admin")
    public String adminReservationPage() {
        return "admin/management";
    }

    @GetMapping("/payments/success")
    public String paymentSuccessPage() {
        return "payments/success";
    }

    @GetMapping("/payments/fail")
    public String paymentFailPage() {
        return "payments/fail";
    }
}
