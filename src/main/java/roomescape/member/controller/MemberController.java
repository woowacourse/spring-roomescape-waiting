package roomescape.member.controller;

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
import roomescape.exception.ExceptionCause;
import roomescape.exception.UnauthorizedException;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.RegistrationRequest;
import roomescape.member.dto.TokenResponse;
import roomescape.member.service.MemberService;

@RestController
public class MemberController {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    public MemberController(TokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @PostMapping("/members")
    public ResponseEntity<Void> registerMember(@Valid @RequestBody RegistrationRequest registrationRequest) {
        memberService.signup(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers(Member member) {
        if (!Role.isAdmin(member.getRole())) {
            throw new UnauthorizedException(ExceptionCause.UNAUTHORIZED_PAGE_ACCESS);
        }

        List<MemberResponse> response = memberService.findAllMembers()
                .stream()
                .map(value -> new MemberResponse(value.getId(), value.getName()))
                .toList();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> processLogin(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {
        TokenResponse tokenResponse = memberService.createToken(loginRequest);

        Cookie cookie = new Cookie("token", tokenResponse.accessToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<MemberResponse> checkLogin(@CookieValue(name = "token", required = false) String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        MemberResponse response = memberService.findMemberById(memberId);
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
