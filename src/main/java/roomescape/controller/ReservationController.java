package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationController {

    @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation";
    }

    @GetMapping("/my-reservations")
    public String myReservationsPage() {
        return "my-reservations";
    }

    @GetMapping("/admin/reservation")
    public String adminReservationPage() {
        return "admin/reservation";
    }
}
