package roomescape.controller.rest;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.member.Member;
import roomescape.global.dto.SessionMember;
import roomescape.service.LoginService;
import roomescape.service.request.LoginRequest;
import roomescape.service.response.LoginCheckResponse;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(final LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody final LoginRequest request, final HttpSession session) {
        final Member member = loginService.login(request);
        session.setAttribute("LOGIN_MEMBER", new SessionMember(member.getId(), member.getName(), member.getRole()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<LoginCheckResponse> loginCheck(final HttpSession httpSession) {
        final SessionMember sessionMember = (SessionMember) httpSession.getAttribute("LOGIN_MEMBER");
        LoginCheckResponse loginCheckResponse = new LoginCheckResponse(sessionMember.name().name());
        return ResponseEntity.ok(loginCheckResponse);
    }
}
