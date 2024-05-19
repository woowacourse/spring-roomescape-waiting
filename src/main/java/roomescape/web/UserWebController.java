package roomescape.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserWebController {

    @GetMapping("/login")
    public String getLoginPage() {
        return "/login";
    }

    @GetMapping("/signup")
    public String getSingUpPage() {
        return "/signup";
    }

    @GetMapping
    public String getPopularThemePage() {
        return "/index";
    }

    @GetMapping("/reservation")
    public String getUserReservationPage() {
        return "/reservation";
    }

    @GetMapping("/reservation-mine")
    public String getUserMyReservationPage() {
        return "/reservation-mine";
    }
}
