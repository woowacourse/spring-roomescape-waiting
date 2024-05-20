package roomescape.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationPageController {

    @GetMapping("/reservation")
    public String getUserPage() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String getReservationMinePage() {
        return "reservation-mine";
    }
}
