package roomescape.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberLoginResponse;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/login")
public class MemberLoginController {

    private final MemberService memberService;
    private final MemberAuthService memberAuthService;

    public MemberLoginController(MemberAuthService memberAuthService, MemberService memberService) {
        this.memberAuthService = memberAuthService;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody MemberLoginRequest memberRequest, HttpServletResponse response) {
        Member member = memberService.findMember(memberRequest);

        memberAuthService.validateAuthentication(member, memberRequest);
        String token = memberAuthService.publishToken(member);
        Cookie cookie = new Cookie("token", token);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/check")
    public ResponseEntity<MemberLoginResponse> check(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String memberName = memberAuthService.extractNameFromPayload(cookies);

        return ResponseEntity.ok(new MemberLoginResponse(memberName));
    }

}
