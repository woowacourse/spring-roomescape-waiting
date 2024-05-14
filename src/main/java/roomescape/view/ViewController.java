package roomescape.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String getMainPage() {
        return "index";
    }
    
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/reservation")
    public String getReservationPage() {
        return "reservation";
    }
}
