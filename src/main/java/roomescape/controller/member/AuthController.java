package roomescape.controller.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.TokenResponse;
import roomescape.controller.member.dto.CookieMemberResponse;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.member.dto.MemberLoginRequest;
import roomescape.domain.Member;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final MemberService memberService;
    private final AuthService authService;

    public AuthController(final MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid final MemberLoginRequest memberLoginRequest,
            final HttpServletResponse response) {
        final Member member = memberService.find(memberLoginRequest);
        final TokenResponse token = authService.createToken(member);

        final Cookie cookie = new Cookie("token", token.assessToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600000);
        response.addCookie(cookie);
        response.setHeader("Keep-Alive", "timeout=60");
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/check")
    public ResponseEntity<CookieMemberResponse> check(final LoginMember loginMember) {
        if (loginMember.id() != null) {
            final Member member = memberService.findMemberById(loginMember.id());
            return ResponseEntity.ok(new CookieMemberResponse(member.getName()));
        }
        return ResponseEntity.ok()
                .build();
    }
}
