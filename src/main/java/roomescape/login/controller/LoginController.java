package roomescape.login.controller;

import javax.naming.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.dto.LoginRequest;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/login")
public class LoginController {

    private static final String TOKEN = "token";
    private final MemberService memberService;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) throws AuthenticationException {
        String token = memberService.createMemberToken(loginRequest);
        ResponseCookie responseCookie = ResponseCookie.from(TOKEN, token)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }

    @GetMapping("/check")
    public MemberNameResponse getLoginMemberName(MemberNameResponse memberNameResponse) {
        return memberNameResponse;
    }
}
