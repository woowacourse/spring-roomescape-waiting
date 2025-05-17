package roomescape.member.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.global.jwt.AuthorizationExtractor;
import roomescape.member.application.service.MemberService;
import roomescape.member.domain.Role;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.TokenRequest;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final MemberService memberService;
    private final AuthorizationExtractor authorizationExtractor;

    public LoginController(MemberService memberService, AuthorizationExtractor authorizationExtractor) {
        this.memberService = memberService;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Auth(Role.GUEST)
    @PostMapping
    public ResponseEntity<Void> login(
            @RequestBody @Valid TokenRequest tokenRequest
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, memberService.createToken(tokenRequest))
                .build();
    }

    @Auth(Role.GUEST)
    @GetMapping("/check")
    public ResponseEntity<MemberResponse> loginCheck(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok().body(
                memberService.findByToken(authorizationExtractor.extract(request))
        );
    }
}
