package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin")
public class AdminPageController {

    @GetMapping
    public String getMainPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String getTimePage() {
        return "admin/time";
    }

    @GetMapping("/themes")
    public String getThemePage() {
        return "admin/theme";
    }

    @GetMapping("/reservation/waiting")
    public String getWaitingPage() {
        return "admin/waiting";
    }
}
