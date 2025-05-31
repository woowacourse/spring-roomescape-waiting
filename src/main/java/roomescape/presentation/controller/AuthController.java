package roomescape.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.AuthService;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.presentation.support.CookieUtils;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        authService.login(loginRequestDto);
        TokenResponseDto tokenResponseDto = authService.createToken(loginRequestDto.email());

        cookieUtils.setCookieForToken(httpServletResponse, tokenResponseDto.token());
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public MemberResponseDto loginCheck(HttpServletRequest httpServletRequest) {
        String tokenFromCookie = cookieUtils.getToken(httpServletRequest);
        return authService.getMemberByToken(tokenFromCookie);
    }
}
