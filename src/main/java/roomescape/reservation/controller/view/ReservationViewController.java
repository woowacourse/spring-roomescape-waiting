package roomescape.reservation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationViewController {

    @GetMapping("/reservation-mine")
    public String getMyReservationPage() {
        return "reservation-mine";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }
}
