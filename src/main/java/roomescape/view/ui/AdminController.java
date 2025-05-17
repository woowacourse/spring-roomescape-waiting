package roomescape.view.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.RequiresRole;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String home() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String reservation() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String time() {
        return "admin/time";
    }

    @GetMapping("/theme")
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String theme() {
        return "admin/theme";
    }
}
