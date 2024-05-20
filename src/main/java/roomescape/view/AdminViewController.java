package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping("/admin")
    public String adminMainPage() {
        return "admin/index";
    }

    @GetMapping("/admin/reservation")
    public String reservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/time")
    public String reservationTimePage() {
        return "admin/time";
    }

    @GetMapping("/admin/theme")
    public String themePage() {
        return "admin/theme";
    }
}
