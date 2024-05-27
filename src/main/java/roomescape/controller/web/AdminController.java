package roomescape.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPage() {
        return "admin/index";
    }

    @GetMapping("/admin/times")
    public String reservationTimeAdminPage() {
        return "admin/time";
    }

    @GetMapping("/admin/reservations")
    public String reservationAdminPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/themes")
    public String themeAdminPage() {
        return "admin/theme";
    }

    @GetMapping("/admin/waitings")
    public String waitingAdminPage() {
        return "admin/waiting";
    }
}
