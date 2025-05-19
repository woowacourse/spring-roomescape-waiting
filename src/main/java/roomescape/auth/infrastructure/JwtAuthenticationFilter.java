package roomescape.auth.infrastructure;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import roomescape.auth.infrastructure.jwt.JwtTokenProvider;
import roomescape.auth.infrastructure.util.CookieManager;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String MEMBER_ID_ATTRIBUTE = "memberId";
    private static final List<String> WHITELIST = List.of("/login", "/logout", "/signup", "/h2-console");


    private final JwtTokenProvider jwtTokenProvider;
    private final CookieManager cookieManager;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final String token = cookieManager.extractLoginToken(request.getCookies());

        if (!StringUtils.hasText(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }

        Long memberId = null;
        try {
            final String subject = jwtTokenProvider.extractPrincipal(token);
            memberId = Long.valueOf(subject);
            request.setAttribute(MEMBER_ID_ATTRIBUTE, memberId);
        } catch (JwtException e) {
            log.debug("토큰을 추출하는 과정에서 문제가 발생했습니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다.");
            return;
        } catch (NumberFormatException e) {
            log.debug("memberId의 형식이 올바르지 않습니다 memberId = {}", memberId, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        final String path = request.getRequestURI();
        return WHITELIST.stream().anyMatch(path::equals);
    }
}
