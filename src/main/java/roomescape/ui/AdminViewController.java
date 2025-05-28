package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import roomescape.auth.annotation.RequiredAdmin;

@RequestMapping("/admin")
@Controller
public class AdminViewController {

    @RequiredAdmin
    @GetMapping
    public String dashboard() {
        return "admin/index";
    }

    @RequiredAdmin
    @GetMapping("/reservation")
    public String adminReservationDashboard() {
        return "admin/reservation-new";
    }

    @RequiredAdmin
    @GetMapping("/time")
    public String adminReservationTimeDashboard() {
        return "admin/time";
    }

    @RequiredAdmin
    @GetMapping("/theme")
    public String adminReservationThemeDashboard() {
        return "admin/theme";
    }

    @RequiredAdmin
    @GetMapping("/waiting")
    public String adminWaitingDashboard() {
        return "admin/waiting";
    }
}
