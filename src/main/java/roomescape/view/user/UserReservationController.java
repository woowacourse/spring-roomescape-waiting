package roomescape.view.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserReservationController {

    @GetMapping("/reservation")
    public String getReservation() {
        return "reservation";
    }

    @GetMapping("reservation-mine")
    public String getMemberReservation() {
        return "reservation-mine";
    }
}
