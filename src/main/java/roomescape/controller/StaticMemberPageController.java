package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticMemberPageController {

    @GetMapping("/")
    public String getHome() {
        return "index";
    }

    @GetMapping("/reservation")
    public String getReservation() {
        return "reservation";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/reservation-mine")
    public String getMemberReservationPage() {
        return "reservation-mine";
    }

    @GetMapping("/waiting")
    public String getWaitingPage() {
        return "waiting";
    }

    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup";
    }
}
