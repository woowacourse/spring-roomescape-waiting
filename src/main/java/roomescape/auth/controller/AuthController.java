package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.constant.AuthConstant;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.AuthenticationCheckResponse;
import roomescape.auth.service.AuthService;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public void login(@RequestBody LoginRequest requestBody, HttpServletResponse response) {
        String accessToken = authService.createToken(requestBody);
        Cookie cookie = new Cookie(AuthConstant.COOKIE_KEY_OF_ACCESS_TOKEN, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/check")
    public AuthenticationCheckResponse authenticationCheck(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return AuthenticationCheckResponse.from(authService.checkAuthenticationStatus(cookies));
    }
}
