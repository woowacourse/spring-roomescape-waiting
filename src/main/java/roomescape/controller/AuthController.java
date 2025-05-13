package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.LoginMember;
import roomescape.dto.member.LoginMemberResponse;
import roomescape.dto.member.LoginRequest;
import roomescape.dto.member.RegistrationRequest;
import roomescape.dto.member.TokenResponse;
import roomescape.exception.UnauthorizedAccessException;
import roomescape.service.LoginService;
import roomescape.service.MemberService;
import roomescape.service.SignupService;

@RestController
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;
    private final MemberService memberService;

    public AuthController(SignupService signupService, LoginService loginService, MemberService memberService) {
        this.signupService = signupService;
        this.loginService = loginService;
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<Void> registerMember(@Valid @RequestBody RegistrationRequest registrationRequest) {
        signupService.signup(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<LoginMemberResponse>> getAllMembers(LoginMember member) {
        if (member.getRole().equalsIgnoreCase("USER")) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        List<LoginMemberResponse> response = memberService.findAllMembers()
                .stream()
                .map(value -> new LoginMemberResponse(value.getId(), value.getName()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> processLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        TokenResponse tokenResponse = loginService.createToken(loginRequest);

        Cookie cookie = new Cookie("token", tokenResponse.accessToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginMemberResponse> checkLogin(@CookieValue(name = "token", required = false) String token) {
        LoginMemberResponse response = loginService.findMemberByToken(token);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> processLogout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
