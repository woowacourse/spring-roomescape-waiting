package roomescape.presentation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class AuthViewController {
    public String login() {
        return "login";
    }
}
