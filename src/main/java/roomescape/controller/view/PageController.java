package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String mainPage() {
        return "/index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "/login";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "/reservation";
    }

    @GetMapping("/reservation-mine")
    public String ReservationMinePage() {
        return "/reservation-mine";
    }
}
