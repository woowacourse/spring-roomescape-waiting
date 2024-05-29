package roomescape.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.LoginRequest;
import roomescape.controller.support.Auth;
import roomescape.security.authentication.Authentication;
import roomescape.service.AuthService;
import roomescape.service.dto.response.MemberResponse;
import roomescape.service.dto.response.TokenResponse;

@RestController
public class AuthController {

    private static final String TOKEN_KEY_NAME = "token";
    private static final String EMPTY_TOKEN = "";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse response = authService.authenticateMember(request.toTokenCreationRequest());
        ResponseCookie cookie = ResponseCookie.from(TOKEN_KEY_NAME, response.token())
                .httpOnly(true)
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public MemberResponse checkLogin(@Auth Authentication authentication) {
        return new MemberResponse(authentication.getId(), authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(TOKEN_KEY_NAME, EMPTY_TOKEN)
                .maxAge(0)
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
