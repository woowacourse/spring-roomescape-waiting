package roomescape.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.CookieManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.CookieHandler;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.dto.member.MemberNameResponseDto;
import roomescape.service.AuthService;

@RestController
public class AuthController {

    private static final String TOKEN_COOKIE_NAME = "token";

    private final AuthService authService;
    private final CookieHandler cookieHandler;

    public AuthController(AuthService authService, CookieHandler cookieHandler) {
        this.authService = authService;
        this.cookieHandler = cookieHandler;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ) {
        String token = authService.publishLoginToken(loginRequestDto);
        Cookie cookie = cookieHandler.createCookie(TOKEN_COOKIE_NAME, token);
        response.addCookie(cookie);
    }

    @GetMapping("/login/check")
    @ResponseStatus(HttpStatus.OK)
    public MemberNameResponseDto checkLogin(
            @CurrentMember LoginInfo loginMember
    ) {
        return new MemberNameResponseDto(loginMember.name());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(
            HttpServletResponse response
    ) {
        Cookie cookie = cookieHandler.createCookie(TOKEN_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
