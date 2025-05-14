package roomescape.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping
    public String displayTopRankedTheme() {
        return "index";
    }

    @GetMapping("/reservation")
    public String displayReservation() {
        return "reservation";
    }

    @GetMapping("/login")
    public String displayLogin() {
        return "login";
    }

    @GetMapping("/signup")
    public String displaySignup() {
        return "signup";
    }
}
