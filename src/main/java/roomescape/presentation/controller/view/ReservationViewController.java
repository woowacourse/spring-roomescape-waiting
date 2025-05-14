package roomescape.presentation.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservation")
public class ReservationViewController {
    @GetMapping
    public String reservationView() {
        return "reservation";
    }
}
