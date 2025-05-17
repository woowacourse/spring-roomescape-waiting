package roomescape.common.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {

    @GetMapping("/reservation")
    public String userReservationDashBoard() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String myReservationDashBoard() {
        return "reservation-mine";
    }
}
