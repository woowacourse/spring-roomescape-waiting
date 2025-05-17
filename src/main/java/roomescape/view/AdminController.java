package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Auth(Role.ADMIN)
    @GetMapping
    public String admin(
    ) {
        return "/admin/index";
    }

    @Auth(Role.ADMIN)
    @GetMapping("/reservation")
    public String reservation(
    ) {
        return "/admin/reservation-new";
    }

    @Auth(Role.ADMIN)
    @GetMapping("/time")
    public String time(
    ) {
        return "/admin/time";
    }

    @Auth(Role.ADMIN)
    @GetMapping("/theme")
    public String theme(
    ) {
        return "/admin/theme";
    }
}
