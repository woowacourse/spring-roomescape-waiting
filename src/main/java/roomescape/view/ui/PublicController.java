package roomescape.view.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicController {

    @GetMapping
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signUp() {
        return "signup";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String reservationMine() {
        return "reservation-mine";
    }

    @GetMapping("/waiting-mine")
    public String waitingMine() {
        return "waiting-mine";
    }
}
