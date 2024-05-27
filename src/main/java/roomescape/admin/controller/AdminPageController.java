package roomescape.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String getAdminHome() {
        return "/admin/index";
    }

    @GetMapping("/time")
    public String getAdminTimePage() {
        return "/admin/time";
    }

    @GetMapping("/theme")
    public String getAdminThemePage() {
        return "/admin/theme";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "/admin/reservation-new";
    }

    @GetMapping("/reservation/waiting")
    public String getReservationWaitingPage() {
        return "/admin/waiting";
    }
}
