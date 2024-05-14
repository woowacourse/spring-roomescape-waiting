package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.api.dto.request.TokenContextRequest;
import roomescape.service.MemberService;
import roomescape.service.dto.output.TokenLoginOutput;
import roomescape.util.TokenProvider;

import java.util.List;

public class CheckLoginInterceptor implements HandlerInterceptor {
    private static final List<String> CHECK_LIST = initializeCheckList();
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenContextRequest tokenContextRequest;

    private static List<String> initializeCheckList() {
        return List.of("/login/check");
    }

    public CheckLoginInterceptor(final MemberService memberService, final TokenProvider tokenProvider, final TokenContextRequest tokenContextRequest) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
        this.tokenContextRequest = tokenContextRequest;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (request.getMethod()
                .equals("GET") && !CHECK_LIST.contains(request.getRequestURI())) {
            return true;
        }
        final String token = tokenProvider.parseToken(request);
        final TokenLoginOutput output = memberService.loginToken(token);
        tokenContextRequest.setTokenLoginOutput(output);
        return true;
    }
}
