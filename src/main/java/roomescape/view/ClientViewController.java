package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientViewController {

    @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }
}
