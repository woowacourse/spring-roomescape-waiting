package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.global.annotation.Auth;
import roomescape.member.role.MemberRole;

@Controller
@Auth(roles = MemberRole.ADMIN)
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    public String mainPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String reservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping({"/time"})
    public String timePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String themePage() {
        return "admin/theme";
    }
}
