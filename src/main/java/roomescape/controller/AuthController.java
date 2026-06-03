package roomescape.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.dto.LoginMemberResponse;
import roomescape.controller.dto.LoginRequest;
import roomescape.controller.dto.SignupRequest;
import roomescape.domain.Member;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.service.AuthService;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginMemberResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        Member member = authService.login(request.loginId(), request.password());
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, member.getId());
        return ResponseEntity.ok(LoginMemberResponse.from(member));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginMemberResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpSession session
    ) {
        Member member = authService.signup(request);
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, member.getId());
        return ResponseEntity.ok(LoginMemberResponse.from(member));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginMemberResponse> me(HttpSession session) {
        Object memberId = session.getAttribute(AuthService.LOGIN_MEMBER_ID);
        if (!(memberId instanceof Long id)) {
            throw new RoomescapeException(DomainErrorCode.UNAUTHENTICATED, "로그인이 필요합니다.");
        }

        return ResponseEntity.ok(LoginMemberResponse.from(authService.getLoginMember(id)));
    }
}
