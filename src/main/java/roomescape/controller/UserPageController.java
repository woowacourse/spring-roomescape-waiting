package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {

    @GetMapping("/reservation-mine")
    public String reservationMine() {
        return "reservation-mine";
    }
}
