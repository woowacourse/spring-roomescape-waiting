package roomescape.member.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {

    @GetMapping("/reservation")
    public String reservation(){
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String myReservation() {
        return "reservation-mine";
    }
}
