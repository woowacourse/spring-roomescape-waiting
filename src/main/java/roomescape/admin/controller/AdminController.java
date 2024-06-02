package roomescape.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String readIndexPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String readReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String readTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String readThemePage() {
        return "admin/theme";
    }

    @GetMapping("/reservation/waiting")
    public String readWaitingPage() {
        return "admin/waiting";
    }
}
