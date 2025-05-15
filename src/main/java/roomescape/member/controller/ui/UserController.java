package roomescape.member.controller.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.member.auth.PermitAll;

@RequiredArgsConstructor
@Controller
public class UserController {

    @PermitAll
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @PermitAll
    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String getMyReservationPage() {
        return "reservation-mine";
    }
}
