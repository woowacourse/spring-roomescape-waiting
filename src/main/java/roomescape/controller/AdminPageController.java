package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String showAdminPage() {
        return "admin/index";
    }

    @GetMapping("/admin/time")
    public String showTimePage() {
        return "admin/time";
    }

    @GetMapping("/admin/reservation")
    public String showReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/theme")
    public String showThemePage() {
        return "admin/theme";
    }
}
