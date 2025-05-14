package roomescape.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.member.domain.MemberRole;

@Controller
@RequestMapping("/admin")
@RequireRole(MemberRole.ADMIN)
public class AdminController {

    @GetMapping
    public String showAdminPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String showAdminReservationsPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String showAdminTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String showAdminThemePage() {
        return "admin/theme";
    }
}
