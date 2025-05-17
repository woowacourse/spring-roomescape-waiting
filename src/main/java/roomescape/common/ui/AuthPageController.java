package roomescape.common.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    public String LoginDashBoard() {
        return "login";
    }

    @GetMapping("/signup")
    public String signUpDashBoard() {
        return "signup";
    }
}
