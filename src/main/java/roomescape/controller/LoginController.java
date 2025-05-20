package roomescape.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.domain.Member;
import roomescape.service.dto.LoginRequest;
import roomescape.service.AuthService;
import roomescape.service.MemberService;

@Controller
public class LoginController {

    private static final String SESSION_KEY = "id";
    private final AuthService authService;
    private final MemberService memberService;

    public LoginController(final AuthService authService, final MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody final LoginRequest loginRequest, final HttpSession session) {
        final Member member = authService.getMemberByEmailAndPassword(loginRequest);
        session.setAttribute(SESSION_KEY, member.getId());
        session.setMaxInactiveInterval(60 * 60);
        authService.updateSessionId(member, session.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpSession session) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginResponse> loginCheck(final HttpSession session) {
        if (session.getAttribute(SESSION_KEY) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final long id = (long) session.getAttribute(SESSION_KEY);
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(memberService.getMemberById(id).getName()));
    }

    record LoginResponse(
            String name
    ) {}
}
