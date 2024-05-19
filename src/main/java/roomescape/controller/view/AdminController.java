package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping
    public String findAdminPage() {
        return "admin/index";
    }

    @GetMapping("/admin/reservation")
    public String findAdminReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/time")
    public String findAdminTimePage() {
        return "admin/time";
    }

    @GetMapping("/admin/theme")
    public String findAdminThemePage() {
        return "admin/theme";
    }
}
