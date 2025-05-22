package roomescape.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String getAdmin() {
        return "redirect:/";
    }

    @GetMapping("/reservation")
    public String getAdminReservation() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String getAdminTime() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String getAdminTheme() {
        return "admin/theme";
    }

    @GetMapping("/waiting")
    public String getAdminWaiting() {
        return "admin/waiting";
    }
}
