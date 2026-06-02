package roomescape.domain.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/popular")
    public String popular() {
        return "popular";
    }

    @GetMapping("/my-reservations")
    public String myReservations() {
        return "my-reservations";
    }
}
