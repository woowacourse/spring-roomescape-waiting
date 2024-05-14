package roomescape.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class MemberPageController {

    @GetMapping
    public String popularThemePage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String userReservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String userLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String userSignupPage() {
        return "signup";
    }
}
