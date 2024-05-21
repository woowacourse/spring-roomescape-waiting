package roomescape.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
        final ResponseCookie cookie = initializeCookie(output.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .build();
    }
    private ResponseCookie initializeCookie(final String token){
        return ResponseCookie.from("token",token)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @GetMapping("/check")
    public ResponseEntity<TokenLoginResponse> checkLogin(final LoginMemberRequest request) {
        return ResponseEntity.ok(new TokenLoginResponse(request.name()));
    }
}
