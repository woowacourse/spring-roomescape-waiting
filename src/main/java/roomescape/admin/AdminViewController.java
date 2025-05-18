package roomescape.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {
    @GetMapping
    public String displayMain() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String displayReservationStatus() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String displayTimeSlot() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String displayThemeSlot() {
        return "admin/theme";
    }
}
