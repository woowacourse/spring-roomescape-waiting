package roomescape.system.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.system.auth.annotation.MemberId;
import roomescape.system.auth.dto.LoginCheckResponse;
import roomescape.system.auth.dto.LoginRequest;
import roomescape.system.auth.jwt.dto.TokenDto;
import roomescape.system.auth.service.AuthService;
import roomescape.system.dto.response.ApiResponse;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(
            @Valid @RequestBody final LoginRequest loginRequest,
            final HttpServletResponse response
    ) {
        final TokenDto tokenInfo = authService.login(loginRequest);
        addCookieToResponse(new Cookie("accessToken", tokenInfo.accessToken()), response);
        addCookieToResponse(new Cookie("refreshToken", tokenInfo.refreshToken()), response);
        return ApiResponse.success();
    }

    @GetMapping("/login/check")
    public ApiResponse<LoginCheckResponse> checkLogin(@MemberId final Long memberId) {
        final LoginCheckResponse response = authService.checkLogin(memberId);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        TokenDto tokenInfo = getTokenFromCookie(request);
        Cookie cookie = new Cookie("accessToken", tokenInfo.accessToken());
        Cookie cookie2 = new Cookie("refreshToken", tokenInfo.refreshToken());
        cookie.setMaxAge(0);
        cookie2.setMaxAge(0);
        addCookieToResponse(cookie, response);
        addCookieToResponse(cookie2, response);
        return ApiResponse.success();
    }

    @GetMapping("/token-reissue")
    public ApiResponse<Void> reissueToken(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final TokenDto requestToken = getTokenFromCookie(request);

        final TokenDto tokenInfo = authService.reissueToken(requestToken.accessToken(), requestToken.refreshToken());
        addCookieToResponse(new Cookie("accessToken", tokenInfo.accessToken()), response);
        addCookieToResponse(new Cookie("refreshToken", tokenInfo.refreshToken()), response);

        return ApiResponse.success();
    }

    private TokenDto getTokenFromCookie(final HttpServletRequest request) {
        String accessToken = "";
        String refreshToken = "";
        for (final Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("accessToken")) {
                accessToken = cookie.getValue();
                cookie.setMaxAge(0);
            }
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
                cookie.setMaxAge(0);
            }
        }
        return new TokenDto(accessToken, refreshToken);
    }

    private void addCookieToResponse(Cookie cookie, final HttpServletResponse response) {
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }
}
