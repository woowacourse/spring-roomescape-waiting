package roomescape.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/reservation")
    public String reservation() {
        return "/reservation";
    }

    @GetMapping
    public String home() {
        return "/index";
    }
}
