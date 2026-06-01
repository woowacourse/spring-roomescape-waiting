package roomescape.controller.client.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/reserve")
    public String reserve() {
        return "forward:/reservation.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/search")
    public String search() {
        return "forward:/search.html";
    }
}
