package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping
    public String showPopularTheme() {
        return "/index";
    }

    @GetMapping("/reservation")
    public String showUserPage() {
        return "/reservation";
    }
}
