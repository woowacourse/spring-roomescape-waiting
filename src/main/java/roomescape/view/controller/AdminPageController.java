package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    @GetMapping
    public String loadAdminPage() {
        return "/admin/index";
    }

    @GetMapping("/reservation")
    public String loadReservationPage() {
        return "/admin/reservation-new";
    }

    @GetMapping("/time")
    public String loadTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String loadThemePage() {
        return "admin/theme";
    }
}
