package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.constant.AuthConstant;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.AuthenticationCheckResponse;
import roomescape.auth.service.AuthService;
import roomescape.global.annotation.Login;
import roomescape.member.model.Member;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public void login(@RequestBody @Valid LoginRequest requestBody, HttpServletResponse response) {
        String accessToken = authService.login(requestBody);
        Cookie cookie = new Cookie(AuthConstant.COOKIE_KEY_OF_ACCESS_TOKEN, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Login
    @GetMapping("/check")
    public AuthenticationCheckResponse authenticationCheck(Member member) {
        return AuthenticationCheckResponse.from(member);
    }
}
