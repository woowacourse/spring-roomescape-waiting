package roomescape.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.api.dto.request.TokenContextRequest;
import roomescape.exception.ForbiddenException;
import roomescape.service.MemberService;
import roomescape.service.dto.output.TokenLoginOutput;
import roomescape.util.TokenProvider;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenContextRequest tokenContextRequest;

    public CheckAdminInterceptor(final MemberService memberService, final TokenProvider tokenProvider, final TokenContextRequest tokenContextRequest) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
        this.tokenContextRequest = tokenContextRequest;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final String token = tokenProvider.parseToken(request);
        final TokenLoginOutput output = memberService.loginToken(token);
        if (output.isAdmin()) {
            tokenContextRequest.setTokenLoginOutput(output);
            return true;
        }
        throw new ForbiddenException(request.getRequestURI());
    }
}

