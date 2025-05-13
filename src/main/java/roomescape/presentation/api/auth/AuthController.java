package roomescape.presentation.api.auth;

import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.auth.AuthService;
import roomescape.application.auth.dto.LoginResult;
import roomescape.presentation.support.methodresolver.AuthInfo;
import roomescape.presentation.support.methodresolver.AuthPrincipal;

@RestController
public class AuthController {

    private static final String TOKEN_COOKIE_KEY = "token";

    private final AuthService authService;
    private final Duration tokenCookieDuration;

    public AuthController(AuthService authService,
                          @Value("${security.jwt.token.expire-duration}") Duration tokenCookieDuration) {
        this.authService = authService;
        this.tokenCookieDuration = tokenCookieDuration;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResult loginResult = authService.login(loginRequest.toServiceParam());
        ResponseCookie jwtCookie = createCookie(TOKEN_COOKIE_KEY, loginResult.token(), tokenCookieDuration);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> loginCheck(@AuthPrincipal AuthInfo authInfo) {
        return ResponseEntity.ok().body(new LoginCheckResponse(authInfo.name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie jwtCookie = createCookie(TOKEN_COOKIE_KEY, "", Duration.ZERO);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) //https 적용전 임시
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }
}
