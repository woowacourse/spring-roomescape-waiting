package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/reservation-mine")
    public String showMyReservationPage() {
        return "reservation-mine";
    }
}
