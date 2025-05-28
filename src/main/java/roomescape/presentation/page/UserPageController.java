package roomescape.presentation.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.auth.AuthRequired;
import roomescape.auth.LoginInfo;
import roomescape.business.model.vo.UserRole;

@Controller
public class UserPageController {

    @GetMapping("/")
    @AuthRequired
    public String getHomePage(LoginInfo loginInfo) {
        if (loginInfo.userRole() == UserRole.USER) {
            return "index";
        }
        return "redirect:/admin";
    }

    @GetMapping("/reservation")
    @AuthRequired
    public String getReservationPage() {
        return "reservation";
    }

    @GetMapping("/reservation/mine")
    @AuthRequired
    public String getMyReservationPage() {
        return "reservation-mine";
    }
}
