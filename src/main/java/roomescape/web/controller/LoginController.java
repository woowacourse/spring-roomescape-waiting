package roomescape.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.login.LoginCheckResponse;
import roomescape.dto.login.LoginMember;
import roomescape.dto.login.LoginRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.token.TokenDto;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

@RestController
public class LoginController {

    private final AuthService authService;
    private final MemberService memberService;

    public LoginController(AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        TokenDto loginToken = authService.login(request);
        ResponseCookie cookie = createResponseCookie(loginToken);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(LoginMember loginMember) {
        MemberResponse memberResponse = memberService.getMemberById(loginMember.id());
        LoginCheckResponse response = new LoginCheckResponse(memberResponse.name());
        return ResponseEntity.ok(response);
    }

    private ResponseCookie createResponseCookie(TokenDto loginToken) {
        return ResponseCookie
                .from("token", loginToken.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
    }
}
