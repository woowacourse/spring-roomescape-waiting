package roomescape.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    public String getAdmin() {
        return "redirect:/";
    }

    @GetMapping("/reservation")
    public String getAdminReservation() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String getTimes() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String getThemes() {
        return "admin/theme";
    }
}
