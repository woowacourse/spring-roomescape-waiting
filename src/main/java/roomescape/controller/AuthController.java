package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.response.MemberResponse;

@RestController
public class AuthController {

    public static final String TOKEN_COOKIE_NAME = "token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> checkLogin(AuthInfo authInfo) {
        if (authInfo == null) {
            return ResponseEntity.noContent().build();
        }

        MemberResponse memberResponse = MemberResponse.from(authInfo);
        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_COOKIE_NAME, null)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
