package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.service.MemberService;

@RestController
public class LoginController {
    public static final String COOKIE_NAME = "token";

    MemberService memberService;

    public LoginController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody final TokenRequest tokenRequest,
                                               HttpServletResponse response) {
        TokenResponse tokenResponse = memberService.createToken(tokenRequest);
        response.addCookie(createCookie(tokenResponse));
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> authorizeLogin(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        final String token = Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("토큰이 존재하지 않습니다"));
        final MemberResponse response = memberService.findMemberByToken(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse response) {
        final Cookie cookie = createEmptyCookie();
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> readMembers() {
        List<MemberResponse> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }

    private Cookie createCookie(final TokenResponse tokenResponse) {
        final Cookie cookie = new Cookie(COOKIE_NAME, tokenResponse.accessToken());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private Cookie createEmptyCookie() {
        final Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
