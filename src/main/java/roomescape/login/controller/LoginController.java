package roomescape.login.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.login.dto.LoginRequest;
import roomescape.member.MemberArgumentResolver;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.dto.MemberRequest;
import roomescape.member.service.MemberService;

import javax.naming.AuthenticationException;

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
    public ResponseEntity<MemberNameResponse> getLoginMemberName(
            @MemberArgumentResolver MemberRequest memberRequest) {
        return ResponseEntity.ok(new MemberNameResponse(memberRequest.toLoginMember()));
    }
}
