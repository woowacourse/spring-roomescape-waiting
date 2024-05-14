package roomescape.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {

    @GetMapping("/signup")
    public String userSignUpPage() {
        return "signup";
    }

    @GetMapping("/login")
    public String userLoginPage() {
        return "login";
    }

    @GetMapping("/reservation")
    public String userReservationPage() {
        return "reservation";
    }

    @GetMapping("/")
    public String popularPage() {
        return "index";
    }
}
