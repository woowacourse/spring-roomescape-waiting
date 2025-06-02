package roomescape.presentation.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class UserPageController {

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("reservation-mine")
    public String reservationMine() {
        return "reservation-mine";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
