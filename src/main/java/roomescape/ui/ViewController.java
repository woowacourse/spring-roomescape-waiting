package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import roomescape.auth.annotation.RequiredAdmin;

@Controller
public class ViewController {

    private static final String A = "a";

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

    @RequiredAdmin
    @GetMapping("/admin/waiting")
    public String adminWaitingDashboard() {
        return "admin/waiting";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String myReservations() {
        return "reservation-mine";
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
