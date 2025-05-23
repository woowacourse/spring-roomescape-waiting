package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservation")
public class ReservationViewController {

    @GetMapping
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/mine")
    public String mine() {
        return "reservation-mine";
    }
}
