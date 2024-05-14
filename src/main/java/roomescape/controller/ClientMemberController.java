package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.CookieAuthorizationExtractor;
import roomescape.domain.Member;
import roomescape.domain.dto.*;
import roomescape.service.MemberService;

@RestController
public class ClientMemberController {
    private final AuthorizationExtractor<String> authorizationExtractor;
    private final MemberService memberService;

    public ClientMemberController(final MemberService memberService) {
        this.authorizationExtractor = new CookieAuthorizationExtractor();
        this.memberService = memberService;
    }

    @PostMapping("/members")
    ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        final SignupResponse signupResponse = memberService.createUser(signupRequest);
        setCookie(response, signupResponse.accessToken());
        return ResponseEntity.ok(signupResponse);
    }

    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        final TokenResponse tokenResponse = memberService.login(loginRequest);
        setCookie(response, tokenResponse.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    ResponseEntity<LoginResponse> loginCheck(Member member) {
        final LoginResponse loginResponse = LoginResponse.from(member);
        return ResponseEntity.ok(loginResponse);
    }

    private void setCookie(final HttpServletResponse response, final String accessToken) {
        Cookie cookie = new Cookie(authorizationExtractor.TOKEN_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
