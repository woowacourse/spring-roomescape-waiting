package roomescape.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.annotation.CheckRole;
import roomescape.global.Role;

@Controller
public class MainPageController {

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup";
    }

    @GetMapping("/reservation")
    public String mainPage() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    @CheckRole({Role.USER, Role.ADMIN})
    public String myReservationPage() {
        return "reservation-mine";
    }
}
