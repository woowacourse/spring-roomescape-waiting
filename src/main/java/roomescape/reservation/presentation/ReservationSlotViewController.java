package roomescape.reservation.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationSlotViewController {

    @GetMapping("/")
    public String home() {
        return "times";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
