package roomescape.controller;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.User;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AccessTokenResponse;
import roomescape.dto.response.UserProfileResponse;
import roomescape.service.AuthService;

@RestController
public class AuthController {

    private static final String TOKEN_NAME_FIELD = "token";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest loginRequest
    ) {
        AccessTokenResponse accessTokenResponse = authService.login(loginRequest);
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, accessTokenResponse.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(false)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public UserProfileResponse checkLogin(User user) {
        return new UserProfileResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_NAME_FIELD, "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.ofDays(0))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
