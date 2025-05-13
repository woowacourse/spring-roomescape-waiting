package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;

@Controller
public class UserController {

    @Auth(Role.USER)
    @GetMapping("/reservation")
    public String reservation(
    ) {
        return "/reservation";
    }

    @Auth(Role.GUEST)
    @GetMapping("/login")
    public String login(
    ) {
        return "/login";
    }

    @Auth(Role.GUEST)
    @GetMapping("/signup")
    public String signUp(
    ) {
        return "/signup";
    }

    @Auth(Role.GUEST)
    @GetMapping("/")
    public String index(
    ) {
        return "/index";
    }
}
