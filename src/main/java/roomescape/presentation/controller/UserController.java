package roomescape.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class UserController {

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping(value = "reservation-mine")
    public String reservationMine() {
        return "reservation-mine";
    }
}
