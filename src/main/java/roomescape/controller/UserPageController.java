package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import roomescape.controller.member.dto.LoginMember;

@Controller
public class UserPageController {

    @GetMapping
    public String showPopularThemePage() {
        return "/index";
    }

    @GetMapping("/reservation")
    public String showUserPage() {
        return "/reservation";
    }

    @GetMapping("/login")
    public String showLoginPage(final LoginMember loginMember) {
        if (loginMember != null) {
            return showPopularThemePage();
        }
        return "/login";
    }

    @PostMapping("/logout")
    public String logout(final HttpServletResponse response) {
        final Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return showPopularThemePage();
    }

    @GetMapping("/signup")
    public String signup() {
        return "/signup";
    }

    @GetMapping("/reservation-mine")
    public String mine() {
        return "/reservation-mine";
    }
}
