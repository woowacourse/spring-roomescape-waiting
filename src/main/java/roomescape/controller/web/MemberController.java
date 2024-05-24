package roomescape.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/reservations")
    public String reservationMemberPage() {
        return "reservation";
    }

    @GetMapping
    public String mainMemberPage() {
        return "index";
    }

    @GetMapping("/reservation-mine")
    public String reservationMinePage() {
        return "reservation-mine";
    }
}
