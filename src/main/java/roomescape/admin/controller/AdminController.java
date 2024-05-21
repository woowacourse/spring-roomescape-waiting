package roomescape.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String readAdmin() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String readReservations() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String readTimes() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String readTheme() {
        return "admin/theme";
    }
}
