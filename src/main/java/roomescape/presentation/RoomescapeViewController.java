package roomescape.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomescapeViewController {

    @GetMapping("/")
    public String home() {
        return "times";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
