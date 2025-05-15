package roomescape.global.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicController {

    @GetMapping
    public String showHomePage(){
        return "index";
    }

    @GetMapping("/reservation")
    public String showReservationPage(){
        return "reservation";
    }

    @GetMapping("/signup")
    public String showSignupPage(){
        return "signup";
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "login";
    }
}
