package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.config.annotation.Authority;
import roomescape.domain.Role;

@Controller
public class GeneralViewController {

    @GetMapping
    public String getHomePage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/reservation-mine")
    @Authority(Role.GENERAL)
    public String getReservationMinePage() {
        return "reservation-mine";
    }
}
