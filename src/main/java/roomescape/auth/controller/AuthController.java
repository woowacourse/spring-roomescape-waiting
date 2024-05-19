package roomescape.auth.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.auth.dto.*;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.TokenCookieService;

@Controller
public class AuthController {

    private final AuthService authService;
    private final TokenCookieService tokenCookieService;
    @Value("${jwt.expired-period}")
    private long expiredPeriod;

    public AuthController(AuthService authService, TokenCookieService tokenCookieService) {
        this.authService = authService;
        this.tokenCookieService = tokenCookieService;
    }

    @GetMapping("/login")
    public String readLoginPage() {
        return "/login";
    }

    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);
        String cookie = tokenCookieService.createTokenCookie(data.accessToken(), expiredPeriod);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .build();
    }

    @ResponseBody
    @GetMapping("/login/check")
    public LoginCheckResponse checkLogin(LoginMember loginMember) {
        return authService.checkLogin(loginMember);
    }

    @ResponseBody
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String cookie = tokenCookieService.createTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .build();
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @ResponseBody
    @PostMapping("/signup")
    public LoginCheckResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }
}
