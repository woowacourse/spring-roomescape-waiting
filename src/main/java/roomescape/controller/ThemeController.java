package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThemeController {

    @GetMapping("/theme")
    public String themePage() {
        return "theme";
    }

    @GetMapping("/admin/theme")
    public String adminThemePage() {
        return "admin/theme";
    }
}

