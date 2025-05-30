package roomescape.presentation.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.LoginMember;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@RequiredArgsConstructor
@RequestMapping("/login")
@RestController
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping
    public void login(@RequestBody @Valid final LoginRequestDto loginRequestDto,
                      final HttpServletResponse httpServletResponse) {
        final TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);
        cookieUtils.setCookieForToken(httpServletResponse, tokenResponseDto.token());
    }

    @GetMapping("/check")
    public LoginMember loginCheck(final HttpServletRequest httpServletRequest) {
        final String tokenFromCookie = cookieUtils.getToken(httpServletRequest);
        return authService.getMemberByToken(tokenFromCookie);
    }
}
