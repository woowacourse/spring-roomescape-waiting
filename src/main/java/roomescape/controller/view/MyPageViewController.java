package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservation-mine")
public class MyPageViewController {

    @GetMapping
    public String getMyReservation() {
        return "reservation-mine";
    }
}
