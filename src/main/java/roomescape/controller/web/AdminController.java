package roomescape.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPage() {
        return "admin/index";
    }

    @GetMapping("/admin/time")
    public String reservationTimeAdminPage() {
        return "admin/time";
    }

    @GetMapping("/admin/reservation")
    public String reservationAdminPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/theme")
    public String themeAdminPage() {
        return "admin/theme";
    }
}
