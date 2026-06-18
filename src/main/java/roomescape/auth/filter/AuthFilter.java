package roomescape.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import roomescape.auth.session.SessionUtils;
import roomescape.member.Member;
import roomescape.member.MemberDao;

@Component
public class AuthFilter extends OncePerRequestFilter {

    /**
     * 기본은 인증 필수(default-deny) — 새로 추가되는 보호 엔드포인트는 자동으로 인증 대상이 된다.
     * 아래 공용 경로만 통과시킨다. /admin·/manager는 각자 역할 필터가 (로그인+권한) 검사를 완결하므로 여기선 건너뛴다.
     */
    private static final List<String> PUBLIC_PATTERNS = List.of(
            // 공용 페이지
            "/", "/login", "/signup", "/reservation", "/my-reservations", "/payment/**",
            // 인증·가입 API
            "/members", "/logout",
            // 공개 조회 API
            "/themes", "/themes/**", "/stores", "/payments/config",
            // 역할 전용 도메인(자체 필터가 보호) + 인프라
            "/admin", "/admin/**", "/manager", "/manager/**", "/error", "/favicon.ico"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final MemberDao memberDao;
    private final ObjectMapper objectMapper;

    public AuthFilter(MemberDao memberDao, ObjectMapper objectMapper) {
        this.memberDao = memberDao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("memberId") == null) {
            sendUnauthorized(request, response);
            return;
        }
        Long memberId = SessionUtils.parseMemberId(session.getAttribute("memberId"));
        if (memberId == null) {
            sendUnauthorized(request, response);
            return;
        }
        Member member = memberDao.findById(memberId).orElse(null);
        if (member == null) {
            sendUnauthorized(request, response);
            return;
        }
        request.setAttribute(SessionUtils.LOGIN_MEMBER_ATTRIBUTE, member);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        problem.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
