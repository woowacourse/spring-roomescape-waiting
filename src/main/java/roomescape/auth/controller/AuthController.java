package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.auth.service.AuthService;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.CookieUtil;
import roomescape.global.auth.LoginMember;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid final LoginRequest loginRequest,
            final HttpServletResponse response
    ) {
        final TokenResponse token = authService.createToken(loginRequest);
        final Cookie cookie = CookieUtil.setTokenInfo(token.accessToken());
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthMember final LoginMember member,
            final HttpServletResponse response
    ) {
        final Cookie cookie = CookieUtil.setTokenInfo(null);
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> loginCheck(@AuthMember final LoginMember member) {
        final LoginCheckResponse response = new LoginCheckResponse(member.name());
        return ResponseEntity.ok().body(response);
    }
}
