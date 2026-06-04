package roomescape.presentation.reservation;

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
