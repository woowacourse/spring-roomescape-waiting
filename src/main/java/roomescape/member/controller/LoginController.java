package roomescape.member.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SignupRequest;
import roomescape.member.service.MemberService;

@Controller
public class LoginController {

    private static final String SESSION_KEY = "memberId";

    private final MemberService memberService;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<MemberResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Member member = memberService.login(request);
        session.setAttribute(SESSION_KEY, member.getId());
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
