package roomescape.admin.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin/time")
    public String time() {
        return "time";
    }

    @GetMapping("/admin/theme")
    public String theme() {
        return "theme";
    }
}
