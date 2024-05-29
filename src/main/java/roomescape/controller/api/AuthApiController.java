package roomescape.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.api.dto.request.LoginMemberRequest;
import roomescape.controller.api.dto.request.MemberCreateRequest;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.controller.api.dto.response.MemberCreateResponse;
import roomescape.controller.api.dto.response.TokenLoginResponse;
import roomescape.service.MemberService;
import roomescape.service.dto.output.MemberCreateOutput;
import roomescape.service.dto.output.MemberLoginOutput;

import java.net.URI;

@RestController
public class AuthApiController {
    private final MemberService memberService;

    public AuthApiController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<MemberCreateResponse> createMember(@RequestBody final MemberCreateRequest request) {
        final MemberCreateOutput output = memberService.createMember(request.toInput());
        return ResponseEntity.created(URI.create("/reservations/" + output.id()))
                .body(MemberCreateResponse.from(output));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody final MemberLoginRequest memberLoginRequest, final HttpServletResponse response) {
        final MemberLoginOutput output = memberService.loginMember(memberLoginRequest.toInput());
        final ResponseCookie cookie = initializeCookie(output.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie initializeCookie(final String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<TokenLoginResponse> checkLogin(final LoginMemberRequest request) {
        return ResponseEntity.ok(new TokenLoginResponse(request.name()));
    }
}
