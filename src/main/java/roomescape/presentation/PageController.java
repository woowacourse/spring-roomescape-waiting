package roomescape.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class PageController {

    @GetMapping("/admin")
    public String home() {
        return "admin/index";
    }

    @GetMapping("/admin/reservation")
    public String getAdminReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/admin/time")
    public String getReservationTimePage() {
        return "admin/time";
    }

    @GetMapping("/admin/theme")
    public String getThemePage() {
        return "admin/theme";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signup";
    }

    @GetMapping("/reservation-mine")
    public String getMyReservationPage() {
        return "reservation-mine";
    }
}
