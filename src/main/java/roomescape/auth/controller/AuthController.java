package roomescape.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.global.auth.annotation.MemberId;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.dto.response.ApiResponse;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody final LoginRequest loginRequest, final HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginRequest);
        addTokensToCookie(tokenDto, response);

        return ApiResponse.success();
    }

    @GetMapping("/login/check")
    public ApiResponse<LoginCheckResponse> checkLogin(@MemberId final Long memberId) {
        LoginCheckResponse response = authService.checkLogin(memberId);
        return ApiResponse.success(response);
    }

    @GetMapping("/token-reissue")
    public ApiResponse<Void> reissueToken(final HttpServletRequest request, final HttpServletResponse response) {
        TokenDto requestToken = getTokenFromCookie(request);

        TokenDto tokenInfo = authService.reissueToken(requestToken.accessToken(), requestToken.refreshToken());
        addTokensToCookie(tokenInfo, response);

        return ApiResponse.success();
    }

    private TokenDto getTokenFromCookie(final HttpServletRequest request) {
        String accessToken = "";
        String refreshToken = "";
        for (Cookie cookie : request.getCookies()) {
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

    private void addTokensToCookie(TokenDto tokenInfo, HttpServletResponse response) {
        addTokenToCookie("accessToken", tokenInfo.accessToken(), response);
        addTokenToCookie("refreshToken", tokenInfo.refreshToken(), response);
    }

    private void addTokenToCookie(String cookieName, String token, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }
}
