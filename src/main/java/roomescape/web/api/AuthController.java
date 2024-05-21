package roomescape.web.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.MemberService;
import roomescape.service.dto.request.member.LoginRequest;
import roomescape.service.dto.response.member.MemberResponse;
import roomescape.service.security.JwtProvider;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private static final String TOKEN_COOKIE_KEY_NAME = "token";

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request) {
        String token = memberService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, token)
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = createEmptyTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> checkAuthenticated(
            @CookieValue(value = TOKEN_COOKIE_KEY_NAME, defaultValue = "") String token
    ) {
        MemberResponse response = new MemberResponse(jwtProvider.extractId(token), jwtProvider.extractName(token));
        return ResponseEntity.ok().body(response);
    }

    private ResponseCookie createEmptyTokenCookie() {
        return ResponseCookie.from(TOKEN_COOKIE_KEY_NAME, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
    }
}
