package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.config.annotation.Authority;
import roomescape.domain.Role;


@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    @Authority(Role.ADMIN)
    public String getHomePage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    @Authority(Role.ADMIN)
    public String getReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    @Authority(Role.ADMIN)
    public String getTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    @Authority(Role.ADMIN)
    public String getThemePage() {
        return "admin/theme";
    }

    @GetMapping("/waiting")
    @Authority(Role.ADMIN)
    public String getWaitingPage() {
        return "admin/waiting";
    }
}
