package roomescape.presentation.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.auth.AuthRequired;
import roomescape.auth.Role;
import roomescape.business.model.vo.UserRole;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public String getMainPage() {
        return "admin/index";
    }

    @GetMapping("/admin/reservation")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public String getReservationPage() {
        return "admin/reservation";
    }

    @GetMapping("/admin/time")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public String getReservationTimePage() {
        return "admin/time";
    }

    @GetMapping("/admin/theme")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public String getThemePage() {
        return "admin/theme";
    }

    @GetMapping("/admin/waiting")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public String getWaitingPage() {
        return "admin/waiting";
    }
}
