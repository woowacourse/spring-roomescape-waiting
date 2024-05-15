package roomescape.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontPageController {

    @GetMapping("/reservation")
    public String reservation(){
        return "/reservation";
    }

    @GetMapping("/reservation-mine")
    public String reservationMine() {
        return "/reservation-mine";
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }
}
