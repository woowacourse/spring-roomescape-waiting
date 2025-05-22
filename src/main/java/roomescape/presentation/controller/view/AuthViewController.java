package roomescape.presentation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class AuthViewController {

    @GetMapping
    public String login() {
        return "login";
    }
}
