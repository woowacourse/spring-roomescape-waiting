package roomescape.waiting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/waiting")
public class WaitingAdminViewController {

    @GetMapping()
    public String getWaiting() {
        return "admin/waiting";
    }
}
