package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String getHome() {
        return "home";
    }

    @GetMapping("/reservation")
    public String getReservation() {
        return "reservation";
    }

    @GetMapping("/reservation/me")
    public String getMyReservation() {
        return "my-reservation";
    }

    @GetMapping("/admin")
    public String getAdminHome() {
        return "admin-home";
    }

    @GetMapping("/admin/reservation")
    public String getAdminReservation() {
        return "admin-reservation";
    }

    @GetMapping("/admin/time")
    public String getAdminTime() {
        return "admin-time";
    }

    @GetMapping("/admin/theme")
    public String getAdminTheme() {
        return "admin-theme";
    }
}
