package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.service.AuthService;

@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final long COOKIE_MAX_AGE_SECONDS = 3600;
    private static final String USERNAME_FIELD = "email";
    private static final String PASSWORD_FIELD = "password";

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> cookieLogin(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter(USERNAME_FIELD);
        String password = request.getParameter(PASSWORD_FIELD);
        Member member = authService.checkValidLogin(email, password);
        String token = jwtTokenProvider.createToken(member.getId());

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/token")
    public ResponseEntity<TokenResponse> tokenLogin(
            @RequestBody LoginRequest loginRequest) {
        Member member = authService.checkValidLogin(loginRequest.email(), loginRequest.password());
        String token = jwtTokenProvider.createToken(member.getId());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getCurrentMember(@LoginMember Long memberId) {
        Member member = authService.findCurrentMember(memberId);
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
}
