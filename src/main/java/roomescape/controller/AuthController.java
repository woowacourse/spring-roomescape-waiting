package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.LoginMemberRequest;
import roomescape.controller.response.CheckLoginUserResponse;
import roomescape.controller.response.LoginMemberResponse;
import roomescape.service.MemberService;
import roomescape.service.result.MemberResult;

@Controller
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieProvider cookieProvider;
    private final MemberService memberService;

    public AuthController(final JwtTokenProvider jwtTokenProvider, final CookieProvider cookieProvider, final MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieProvider = cookieProvider;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginMemberResponse> login(@Valid @RequestBody LoginMemberRequest loginMemberRequest, HttpServletResponse response) {
        MemberResult memberResult = memberService.login(loginMemberRequest.toServiceParam());
        String token = jwtTokenProvider.createToken(memberResult);

        response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.create(token).toString());
        return ResponseEntity.ok().body(LoginMemberResponse.from(memberResult));
    }

    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginUserResponse> loginCheck(@CookieValue(value = "token", required = false) Cookie cookie) {
        if (cookie == null) {
            return ResponseEntity.ok().build();
        }
        String token = cookieProvider.extractTokenFromCookie(cookie);
        Long id = jwtTokenProvider.extractIdFromToken(token);
        return ResponseEntity.ok().body(CheckLoginUserResponse.from(memberService.findById(id)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("token") Cookie cookie, HttpServletResponse response) {
        response.setHeader(HttpHeaders.SET_COOKIE, cookieProvider.invalidate(cookie).toString());
        return ResponseEntity.ok().build();
    }
}
