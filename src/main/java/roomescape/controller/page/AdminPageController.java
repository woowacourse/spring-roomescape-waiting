package roomescape.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    @GetMapping
    public String findAdminPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String findAdminReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String findAdminReservationTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String findAdminThemePage() {
        return "admin/theme";
    }
}
