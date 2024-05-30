package roomescape.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {
    @GetMapping("/reservation-mine")
    public String myReservation() {
        return "reservation-mine";
    }
}
