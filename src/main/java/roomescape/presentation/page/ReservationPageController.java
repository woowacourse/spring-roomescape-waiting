package roomescape.presentation.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationPageController {

    @GetMapping("/reservation")
    public String getUserReservationPage() {
        return "reservation";
    }
}
