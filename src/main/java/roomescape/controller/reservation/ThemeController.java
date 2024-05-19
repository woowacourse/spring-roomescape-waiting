package roomescape.controller.reservation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThemeController {

    @GetMapping("/")
    public String popularThemesPage() {
        return "index";
    }
}
