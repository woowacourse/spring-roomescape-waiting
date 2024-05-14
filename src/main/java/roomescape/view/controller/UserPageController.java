package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {
    @GetMapping("/reservation")
    public String loadUserReservationPage() {
        return "/reservation";
    }

    @GetMapping("/login")
    public String loadLoginPage() {
        return "/login";
    }
}
