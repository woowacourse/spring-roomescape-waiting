package roomescape.presentation.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.auth.AuthService;
import roomescape.application.auth.dto.LoginRequest;
import roomescape.application.auth.dto.LoginResponse;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.MemberDto;
import roomescape.infrastructure.AuthenticatedMemberId;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Cookie cookie = authService.createCookie(loginRequest);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponse> checkLogin(@AuthenticatedMemberId MemberIdDto memberIdDto) {
        MemberDto memberDto = authService.getMemberById(memberIdDto.id());
        LoginResponse loginResponse = LoginResponse.from(memberDto);
        return ResponseEntity.ok(loginResponse);
    }
}
