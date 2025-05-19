package roomescape.view.ui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.RequiresRole;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @GetMapping
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String home() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    @RequiresRole(authRoles = AuthRole.ADMIN)
    public String reservation() {
        log.info("AdminController.reservation() called");

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
