package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.global.auth.annotation.Admin;

@Controller
public class AdminPageController {

    @Admin
    @GetMapping("/admin")
    public String showAdminPage() {
        return "admin/index";
    }

    @Admin
    @GetMapping("/admin/reservation")
    public String showAdminReservationPage() {
        return "admin/reservation-new";
    }

    @Admin
    @GetMapping("/admin/time")
    public String showAdminTimePage() {
        return "admin/time";
    }

    @Admin
    @GetMapping("/admin/theme")
    public String showAdminThemePage() {
        return "admin/theme";
    }
}
