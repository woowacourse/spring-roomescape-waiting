package roomescape.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
    @GetMapping("/admin")
    public String getAdminPage() {
        return "/admin/index";
    }

    @GetMapping("/admin/reservation")
    public String getAdminReservationPage() {
        return "/admin/reservation-new";
    }

    @GetMapping("/admin/time")
    public String getTimePage() {
        return "/admin/time";
    }

    @GetMapping("/admin/theme")
    public String getThemePage() {
        return "/admin/theme";
    }
}
