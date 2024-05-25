package roomescape.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.security.exception.UnauthorizedException;
import roomescape.service.AuthService;
import roomescape.service.MemberService;
import roomescape.util.CookieUtil;

@RestController
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    public AuthController(AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public void login(@RequestBody @Valid LoginRequest loginRequest,
                      HttpServletResponse response) {
        MemberResponse memberResponse = authService.validatePassword(loginRequest);
        String token = authService.createToken(memberResponse.id());
        CookieUtil.setTokenCookie(response, token);
    }

    @GetMapping("/login/check")
    public MemberResponse checkLogin(HttpServletRequest request) {
        String token = CookieUtil.extractTokenFromCookie(request)
                .orElseThrow(UnauthorizedException::new);

        Long id = authService.getMemberIdByToken(token);
        return memberService.getById(id);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        CookieUtil.clearTokenCookie(response);
    }
}
