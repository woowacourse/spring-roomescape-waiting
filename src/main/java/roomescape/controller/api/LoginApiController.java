package roomescape.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.controller.api.dto.response.TokenLoginResponse;
import roomescape.service.MemberService;
import roomescape.service.dto.output.MemberLoginOutput;

@RestController
@RequestMapping("/login")
public class LoginApiController {
    private final MemberService memberService;

    public LoginApiController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody final MemberLoginRequest memberLoginRequest, final HttpServletResponse response) {
        final MemberLoginOutput output = memberService.loginMember(memberLoginRequest.toInput());
        initializeCookie(response, output);
        return ResponseEntity.ok()
                .build();
    }

    private void initializeCookie(final HttpServletResponse response, final MemberLoginOutput output) {
        final Cookie cookie = new Cookie("token", output.token());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/check")
    public ResponseEntity<TokenLoginResponse> checkLogin(final LoginMemberRequest request) {
        return ResponseEntity.ok(new TokenLoginResponse(request.name()));
    }
}
