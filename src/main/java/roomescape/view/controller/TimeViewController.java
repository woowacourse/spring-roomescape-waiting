package roomescape.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimeViewController {
    @GetMapping("/time")
    public String timePage() {
        return "admin/time";
    }
}
