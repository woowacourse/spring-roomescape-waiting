package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimeController {

    @GetMapping("/admin/time")
    public String adminTimePage() {
        return "admin/time";
    }
}
