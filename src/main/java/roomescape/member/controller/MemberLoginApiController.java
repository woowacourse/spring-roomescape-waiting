package roomescape.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.LoginMember;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.service.MemberLoginService;
import roomescape.util.CookieUtils;

@RestController
public class MemberLoginApiController {

    private final MemberLoginService memberLoginService;

    public MemberLoginApiController(MemberLoginService memberLoginService) {
        this.memberLoginService = memberLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                      HttpServletResponse response) {
        String token = memberLoginService.createMemberToken(memberLoginRequest);
        CookieUtils.setCookieByToken(response, token);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/member")
    public ResponseEntity<MemberResponse> findMemberNameByLoginMember(LoginMember loginMember) {
        MemberResponse memberResponse = memberLoginService.findMemberNameByLoginMember(loginMember);

        return ResponseEntity.ok(memberResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        CookieUtils.clearTokenAndCookie(response);
        return ResponseEntity.ok().build();
    }
}
