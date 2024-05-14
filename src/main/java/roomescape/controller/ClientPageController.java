package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientPageController {
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation";
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }
}
