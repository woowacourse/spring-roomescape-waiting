package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {
    @GetMapping("/reservation")
    public String findReservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String findLoginPage() {
        return "login";
    }
}
