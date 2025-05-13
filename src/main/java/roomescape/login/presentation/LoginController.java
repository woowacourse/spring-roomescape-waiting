package roomescape.login.presentation;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.jwt.Token;
import roomescape.login.business.service.LoginService;
import roomescape.login.business.service.TokenCookieService;
import roomescape.login.presentation.request.LoginCheckRequest;
import roomescape.login.presentation.request.LoginRequest;
import roomescape.login.presentation.request.SignupRequest;
import roomescape.login.presentation.response.LoginCheckResponse;

@RestController
public class LoginController {

    private final LoginService loginService;
    private final TokenCookieService tokenCookieService;

    @Value("${security.jwt.token.access.expire-length}")
    private long expiration;

    public LoginController(final LoginService loginService, final TokenCookieService tokenCookieService) {
        this.loginService = loginService;
        this.tokenCookieService = tokenCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody final LoginRequest request) {
        final Token token = loginService.login(request);
        final String cookie = tokenCookieService.createTokenCookie(token.accessToken(), expiration);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(final LoginCheckRequest request) {
        final LoginCheckResponse loginCheckResponse = loginService.checkLogin(request);
        return ResponseEntity.ok(loginCheckResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        final String cookie = tokenCookieService.createTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .build();
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginCheckResponse> signup(@Valid @RequestBody final SignupRequest request) {
        final LoginCheckResponse loginCheckResponse = loginService.signup(request);
        return ResponseEntity.ok(loginCheckResponse);
    }
}
