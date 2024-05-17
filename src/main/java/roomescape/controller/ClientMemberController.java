package roomescape.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.CookieAuthorizationExtractor;
import roomescape.domain.Member;
import roomescape.domain.dto.LoginRequest;
import roomescape.domain.dto.LoginResponse;
import roomescape.domain.dto.SignupRequest;
import roomescape.domain.dto.SignupResponse;
import roomescape.domain.dto.TokenResponse;
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
    ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        final SignupResponse signupResponse = memberService.createUser(signupRequest);
        return ResponseEntity.ok(signupResponse);
    }

    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        final TokenResponse tokenResponse = memberService.login(loginRequest);
        final ResponseCookie cookie = createCookie(tokenResponse.accessToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    ResponseEntity<LoginResponse> loginCheck(Member member) {
        final LoginResponse loginResponse = LoginResponse.from(member);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout() {
        ResponseCookie cookie = createCookie("");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createCookie(final String accessToken) {
        return ResponseCookie.from(authorizationExtractor.TOKEN_NAME, accessToken)
                .httpOnly(true)
                .path("/")
                .build();
    }
}
