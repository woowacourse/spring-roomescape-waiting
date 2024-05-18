package roomescape.auth.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.application.AuthService;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.AuthInformationResponse;
import roomescape.member.domain.Member;

@RestController
public class AuthController {
    private static final String TOKEN_COOKIE_KEY = "token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.createToken(request.email(), request.password());
        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_KEY, token).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<AuthInformationResponse> checkAuthInformation(Member loginMember) {
        return ResponseEntity.ok(new AuthInformationResponse(loginMember.getName()));
    }
}
