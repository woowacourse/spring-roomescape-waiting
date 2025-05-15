package roomescape.presentation.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String getHomePage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String getManageReservationsPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String getManageTimesPage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String getManageThemePage() {
        return "admin/theme";
    }
}
