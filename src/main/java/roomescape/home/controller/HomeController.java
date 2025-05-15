package roomescape.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/reservation")
    public String reservation() {
        return "/reservation";
    }

    @GetMapping("/reservation-mine")
    public String reservationMinePage() {
        return "/reservation-mine";
    }

    @GetMapping
    public String home() {
        return "/index";
    }
}
