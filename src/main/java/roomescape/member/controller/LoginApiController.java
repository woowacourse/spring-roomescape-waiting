package roomescape.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberTokenResponse;
import roomescape.member.login.authentication.AuthenticationPrincipal;
import roomescape.member.login.authorization.AuthorizationHandler;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;

@Controller
@RequestMapping("/login")
public class LoginApiController {

    private final MemberService memberService;
    private final AuthorizationHandler<String> authorizationHandler;

    public LoginApiController(MemberService memberService, TokenAuthorizationHandler tokenAuthorizationHandler) {
        this.memberService = memberService;
        this.authorizationHandler = tokenAuthorizationHandler;
    }

    @PostMapping
    public ResponseEntity<Void> login(
            @RequestBody MemberLoginRequest memberLoginRequest,
            HttpServletResponse httpServletResponse
    ) {
        MemberTokenResponse memberTokenResponse = memberService.createToken(memberLoginRequest);
        authorizationHandler.createCookie(memberTokenResponse.accessToken(), httpServletResponse);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<MemberResponse> checkLogin(
            @AuthenticationPrincipal MemberResponse memberResponse
    ) {
        return ResponseEntity.ok(memberResponse);
    }
}
