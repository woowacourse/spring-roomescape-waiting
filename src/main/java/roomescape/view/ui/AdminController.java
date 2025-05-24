package roomescape.view.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.auth.domain.RequiresRole;

@Controller
@RequestMapping("/admin")
@RequiresRole(authRoles = ADMIN)
public class AdminController {

    @GetMapping
    public String home() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "admin/reservation-new";
    }

    @GetMapping("/waiting")
    public String waiting() {
        return "admin/waiting";
    }

    @GetMapping("/time")
    public String time() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String theme() {
        return "admin/theme";
    }
}
