package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin")
@Controller
public class AdminPageController {

    @GetMapping
    public String mainPage() {
        return "/admin/index";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "/admin/reservation-new";
    }

    @GetMapping("/time")
    public String timePage() {
        return "/admin/time";
    }

    @GetMapping("/theme")
    public String themePage() {
        return "/admin/theme";
    }
}
