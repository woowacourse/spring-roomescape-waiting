package roomescape.auth.web.controller;

import static roomescape.auth.web.controller.response.AuthSuccessCode.LOGIN;
import static roomescape.auth.web.controller.response.AuthSuccessCode.LOGIN_CHECK;
import static roomescape.auth.web.controller.response.AuthSuccessCode.LOGOUT;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.service.AuthService;
import roomescape.auth.web.controller.request.LoginRequest;
import roomescape.auth.web.controller.response.MemberNameResponse;
import roomescape.auth.web.cookie.CookieProvider;
import roomescape.auth.web.resolver.Authenticated;
import roomescape.global.response.ApiResponse;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;
    private final CookieProvider cookieProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        String accessToken = authService.login(request);
        response.addCookie(cookieProvider.createTokenCookie(accessToken));

        return ResponseEntity
                .ok(ApiResponse.success(LOGIN));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        response.addCookie(cookieProvider.createExpiredTokenCookie());

        return ResponseEntity
                .ok(ApiResponse.success(LOGOUT));
    }

    @GetMapping("/login/check")
    public ResponseEntity<ApiResponse<MemberNameResponse>> checkLogin(@Authenticated Long memberId) {
        MemberNameResponse response = authService.checkLogin(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(LOGIN_CHECK, response));
    }
}
