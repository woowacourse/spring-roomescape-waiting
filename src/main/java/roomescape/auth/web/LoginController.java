package roomescape.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.member.web.LoginRequestDto;
import roomescape.member.web.SignupRequestDto;
import roomescape.member.web.MemberResponseDto;
import roomescape.member.MemberService;

@RestController
public class LoginController {
    private final MemberService memberService;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        Member member = memberService.login(request);
        HttpSession session = httpRequest.getSession();
        session.setAttribute("memberId", member.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/members")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/members/me")
    public ResponseEntity<MemberResponseDto> me(@LoginMember Member member) {
        return ResponseEntity.ok(MemberResponseDto.from(member));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}
