package roomescape.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/reservations")
    public String reservationUserPage() {
        return "reservation";
    }

    @GetMapping
    public String mainUserPage() {
        return "index";
    }

    @GetMapping("/reservation-mine")
    public String reservationMinePage() {
        return "reservation-mine";
    }
}
