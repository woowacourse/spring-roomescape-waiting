package roomescape.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.auth.service.AuthService;

@RestController
public class LoginController {
    private final TokenCookieManager tokenCookieManager;
    private final AuthService authService;

    public LoginController(TokenCookieManager tokenCookieManager, AuthService authService) {
        this.tokenCookieManager = tokenCookieManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        String token = authService.createToken(request);
        ResponseCookie cookie = tokenCookieManager.createResponseCookie(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public LoginResponse loginCheck(LoggedInMember member) {
        String name = member.name();
        return new LoginResponse(name);
    }
}
