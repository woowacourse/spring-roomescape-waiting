package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final String clientKey;

    public PageController(@org.springframework.beans.factory.annotation.Value("${toss.client-key:}") String clientKey) {
        this.clientKey = clientKey;
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/reservation")
    public String userPage(org.springframework.ui.Model model) {
        model.addAttribute("clientKey", clientKey);
        return "user";
    }

    @GetMapping("/reservation-lookup")
    public String reservationLookupPage() {
        return "reservation-lookup";
    }

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @GetMapping("/admin-page")
    public String adminPage() {
        return "admin";
    }
}
