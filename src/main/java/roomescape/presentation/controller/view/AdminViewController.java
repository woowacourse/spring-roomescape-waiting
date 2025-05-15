package roomescape.presentation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    public String adminHome() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String timePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String themePage() {
        return "admin/theme";
    }
}
