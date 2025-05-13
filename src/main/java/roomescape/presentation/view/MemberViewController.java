package roomescape.presentation.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberViewController {

    @GetMapping("/login")
    public String login() {
        return "/login";
    }
}
