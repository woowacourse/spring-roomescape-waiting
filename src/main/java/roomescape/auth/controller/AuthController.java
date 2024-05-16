package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.global.auth.annotation.Auth;
import roomescape.global.auth.annotation.MemberId;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.dto.response.ApiResponse;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(@Valid @RequestBody final LoginRequest loginRequest, final HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginRequest);
        addTokensToCookie(tokenDto, response);

        return ApiResponse.success();
    }

    @Auth
    @GetMapping("/login/check")
    public ApiResponse<LoginCheckResponse> checkLogin(@MemberId final Long memberId) {
        LoginCheckResponse response = authService.checkLogin(memberId);
        return ApiResponse.success(response);
    }

    // TODO: 토큰 재발급 자동화 로직 구현
    @GetMapping("/token-reissue")
    public ApiResponse<Void> reissueToken(final HttpServletRequest request, final HttpServletResponse response) {
        TokenDto requestToken = getTokenFromCookie(request);

        TokenDto tokenInfo = authService.reissueToken(requestToken.accessToken(), requestToken.refreshToken());
        addTokensToCookie(tokenInfo, response);

        return ApiResponse.success();
    }

    // TODO: 로그아웃, 회원가입 구현
    private TokenDto getTokenFromCookie(final HttpServletRequest request) {
        String accessToken = "";
        String refreshToken = "";
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(JwtHandler.ACCESS_TOKEN_HEADER_KEY)) {
                accessToken = cookie.getValue();
                cookie.setMaxAge(0);
            }
            if (cookie.getName().equals(JwtHandler.REFRESH_TOKEN_HEADER_KEY)) {
                refreshToken = cookie.getValue();
                cookie.setMaxAge(0);
            }
        }

        return new TokenDto(accessToken, refreshToken);
    }

    private void addTokensToCookie(TokenDto tokenInfo, HttpServletResponse response) {
        addTokenToCookie(JwtHandler.ACCESS_TOKEN_HEADER_KEY, tokenInfo.accessToken(), response);
        addTokenToCookie(JwtHandler.REFRESH_TOKEN_HEADER_KEY, tokenInfo.refreshToken(), response);
    }

    private void addTokenToCookie(String cookieName, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }
}
