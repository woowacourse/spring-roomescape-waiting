package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationTimeViewController {
    @GetMapping("/time")
    public String reservationTimePage() {
        return "admin/time";
    }
}
