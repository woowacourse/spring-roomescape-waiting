package roomescape.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/reservation")
    public String getReservation() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String getMyReservation() {
        return "reservation-mine";
    }
}
