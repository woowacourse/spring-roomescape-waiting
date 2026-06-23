package roomescape.controller.view;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final String clientKey;

    public ViewController(@Value("${toss.client-key:}") String clientKey) {
        this.clientKey = clientKey;
    }

    @GetMapping("/")
    public String getHome() {
        return "home";
    }

    @GetMapping("/reservation")
    public String getReservation(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "reservation";
    }

    @GetMapping("/reservation/me")
    public String getMyReservation(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "my-reservation";
    }

    @GetMapping("/admin")
    public String getAdminHome() {
        return "admin-home";
    }

    @GetMapping("/admin/reservation")
    public String getAdminReservation() {
        return "admin-reservation";
    }

    @GetMapping("/admin/time")
    public String getAdminTime() {
        return "admin-time";
    }

    @GetMapping("/admin/theme")
    public String getAdminTheme() {
        return "admin-theme";
    }
}
