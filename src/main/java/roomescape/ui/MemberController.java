package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String getMyReservationPage() {
        return "reservation-mine";
    }

    @GetMapping("/sign-in")
    public String getSignInPage() {
        return "signin";
    }

    @GetMapping("/sign-up")
    public String getSignUpPage() {
        return "signup";
    }
}
