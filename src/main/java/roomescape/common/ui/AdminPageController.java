package roomescape.common.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String dashboard() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String adminReservationDashboard() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String adminReservationTimeDashboard() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String adminThemeDashboard() {
        return "admin/theme";
    }

    @GetMapping("/reservation/waiting")
    public String adminReservationWaitingDashboard() {
        return "admin/waiting";
    }
}
