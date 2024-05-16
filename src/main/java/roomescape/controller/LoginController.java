package roomescape.controller;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.LoginMember;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.service.MemberService;

@RestController
public class LoginController {

    private static final String COOKIE_NAME = "token";

    private final MemberService memberService;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody TokenRequest tokenRequest) {
        TokenResponse tokenResponse = memberService.createToken(tokenRequest);
        ResponseCookie responseCookie = createCookie(tokenResponse);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(tokenResponse);
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> authorizeLogin(LoginMember loginMember) {
        MemberResponse response = memberService.loginCheck(loginMember);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie emptyCookie = createEmptyCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, emptyCookie.toString())
                .build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> readMembers() {
        List<MemberResponse> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }

    private ResponseCookie createCookie(TokenResponse tokenResponse) {
        return ResponseCookie.from(COOKIE_NAME, tokenResponse.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
    }

    private ResponseCookie createEmptyCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .build();
    }
}
