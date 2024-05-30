package roomescape.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/signup")
public class SignUpPageController {

    @GetMapping
    public String getSignUpPage() {
        return "signup";
    }
}
