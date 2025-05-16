package roomescape.presentation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservation-mine")
public class MemberReservationViewController {

    @GetMapping
    public String getReservationMine() {
        return "reservation-mine";
    }
}
