package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.dto.LoginMemberCheckResponse;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.service.AuthService;
import roomescape.member.service.MemberService;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.member.service.dto.MemberLoginCommand;

@RestController
public class MemberController {

    private static final int LOGIN_COOKIE_MAX_AGE = 3600;

    private final AuthService authService;
    private final MemberService memberService;

    public MemberController(final AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody MemberLoginCommand request) {
        String token = authService.tokenLogin(request);
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(LOGIN_COOKIE_MAX_AGE)
                .build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginMemberCheckResponse> getLoginMember(LoginMemberInfo loginMemberInfo) {
        return ResponseEntity.ok().body(new LoginMemberCheckResponse(loginMemberInfo.name()));
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getMembers() {
        List<MemberResponse> responses = memberService.findAll()
                .stream()
                .map(MemberResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }
}

