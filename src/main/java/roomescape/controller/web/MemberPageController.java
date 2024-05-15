package roomescape.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {

    @GetMapping
    public String getMainPage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/reservation-mine")
    public String getMyReservationPage() {
        return "reservation-mine";
    }
}
