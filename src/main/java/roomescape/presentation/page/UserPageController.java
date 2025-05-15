package roomescape.presentation.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.auth.AuthRequired;

@Controller
public class UserPageController {

    @GetMapping("/")
    @AuthRequired
    public String getPopularPage() {
        return "index";
    }

    @GetMapping("/reservation")
    @AuthRequired
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/reservation/mine")
    @AuthRequired
    public String getMyReservationPage() {return "reservation-mine";}
}
