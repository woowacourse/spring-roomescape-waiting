package roomescape.view.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/signup")
@Controller
public class SignUpController {

    @GetMapping
    public String signUp() {
        return "signup";
    }

}
