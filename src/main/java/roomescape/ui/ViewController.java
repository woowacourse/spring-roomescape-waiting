package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import roomescape.auth.annotation.RequiredAdmin;

@Controller
public class ViewController {

    @GetMapping()
    public String index() {
        return "index";
    }

    @RequiredAdmin
    @GetMapping("/admin")
    public String dashboard() {
        return "admin/index";
    }

    @RequiredAdmin
    @GetMapping("/admin/reservation")
    public String adminReservationDashboard() {
        return "/admin/reservation-new";
    }

    @RequiredAdmin
    @GetMapping("/admin/time")
    public String adminReservationTimeDashboard() {
        return "admin/time";
    }

    @RequiredAdmin
    @GetMapping("/admin/theme")
    public String adminReservationThemeDashboard() {
        return "admin/theme";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
