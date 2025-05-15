package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.member.entity.RoleType;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public String showMain() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    @RoleRequired(roleType = RoleType.ADMIN)
    public String showReservations() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    @RoleRequired(roleType = RoleType.ADMIN)
    public String showTimes() {
        return "admin/time";
    }

    @GetMapping("/theme")
    @RoleRequired(roleType = RoleType.ADMIN)
    public String showThemes() {
        return "admin/theme";
    }
}
