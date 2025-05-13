package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
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
