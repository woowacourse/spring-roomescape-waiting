package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.service.MemberService;
import roomescape.service.dto.output.TokenLoginOutput;
import roomescape.util.TokenProvider;

import java.util.List;

@Component
public class CheckLoginInterceptor implements HandlerInterceptor {
    private static final List<String> CHECK_LIST = initializeCheckList();
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    private static List<String> initializeCheckList() {
        return List.of("/reservations");
    }

    public CheckLoginInterceptor(final MemberService memberService, final TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (request.getMethod()
                .equals("GET") && CHECK_LIST.contains(request.getRequestURI())) {
            return true;
        }
        final String token = tokenProvider.parseToken(request);
        final TokenLoginOutput output = memberService.loginToken(token);
        request.setAttribute("member", output);
        return true;
    }
}
