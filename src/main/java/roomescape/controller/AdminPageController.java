package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String getHome() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String getReservation() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String getReservationTime() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String getTheme() {
        return "admin/theme";
    }
}
