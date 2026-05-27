package roomescape.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/reservations")
    public String reservations() {
        return "forward:/reservations.html";
    }

    @GetMapping("/my-reservations")
    public String myReservations() {
        return "forward:/my-reservations.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }
}
