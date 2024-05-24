package roomescape.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationPageController {

    @GetMapping
    public String getPopularPage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String getReservationSlotPage() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String getMyPage() {
        return "reservation-mine";
    }
}
