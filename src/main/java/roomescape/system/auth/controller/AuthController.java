package roomescape.system.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.system.auth.dto.LoginCheckResponse;
import roomescape.system.auth.dto.LoginRequest;
import roomescape.system.auth.service.AuthService;
import roomescape.system.auth.annotation.MemberId;
import roomescape.system.auth.jwt.dto.TokenDto;
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
        final TokenDto tokenDto = authService.login(loginRequest);
        addTokensToCookie(tokenDto, response);

        return ApiResponse.success();
    }

    @GetMapping("/login/check")
    public ApiResponse<LoginCheckResponse> checkLogin(@MemberId final Long memberId) {
        final LoginCheckResponse response = authService.checkLogin(memberId);
        return ApiResponse.success(response);
    }

    @GetMapping("/token-reissue")
    public ApiResponse<Void> reissueToken(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final TokenDto requestToken = getTokenFromCookie(request);

        final TokenDto tokenInfo = authService.reissueToken(requestToken.accessToken(), requestToken.refreshToken());
        addTokensToCookie(tokenInfo, response);

        return ApiResponse.success();
    }

    // TODO: 로그아웃, 회원가입 구현
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

    private void addTokensToCookie(final TokenDto tokenInfo, final HttpServletResponse response) {
        addTokenToCookie("accessToken", tokenInfo.accessToken(), response);
        addTokenToCookie("refreshToken", tokenInfo.refreshToken(), response);
    }

    private void addTokenToCookie(
            final String cookieName,
            final String token,
            final HttpServletResponse response
    ) {
        final Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }
}
