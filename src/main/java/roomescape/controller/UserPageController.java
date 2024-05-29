package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {
    @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation";
    }

    @GetMapping
    public String bestThemePage() {
        return "index";
    }

    @GetMapping("/reservation-mine")
    public String myReservationPage() {
        return "reservation-mine";
    }
}
