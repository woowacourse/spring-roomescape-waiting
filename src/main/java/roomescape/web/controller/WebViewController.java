package roomescape.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @Value("${toss.client-key}")
    private String tossClientKey;

    @GetMapping("/")
    public String welcome() {
        return "index";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/reservations")
    public String adminReservations() {
        return "admin/reservations";
    }

    @GetMapping("/admin/times")
    public String adminTimes() {
        return "admin/times";
    }

    @GetMapping("/admin/themes")
    public String adminThemes() {
        return "admin/themes";
    }

    @GetMapping("/user")
    public String userHome() {
        return "user/home";
    }

    @GetMapping("/user/reserve")
    public String userReserve(Model model) {
        model.addAttribute("tossClientKey", tossClientKey);
        return "user/reserve";
    }

    @GetMapping("/user/payment/success")
    public String paymentSuccess() {
        return "user/payment-success";
    }

    @GetMapping("/user/payment/fail")
    public String paymentFail() {
        return "user/payment-fail";
    }

    @GetMapping("/user/popular")
    public String userPopular() {
        return "user/popular";
    }

    @GetMapping("/user/my-reservations")
    public String userMyReservations() {
        return "user/my-reservations";
    }
}
