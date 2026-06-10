package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/my-reservations")
    public String myReservations() {
        return "my-reservations";
    }

    @GetMapping("/popular-themes")
    public String popularThemes() {
        return "popular-themes";
    }
}