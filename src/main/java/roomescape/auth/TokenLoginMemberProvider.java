package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.domain.AuthenticatedMember;

@Component
public class TokenLoginMemberProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";

    private final JwtTokenProvider jwtTokenProvider;

    public TokenLoginMemberProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthenticatedMember resolveAndCacheAuthenticatedMember(HttpServletRequest request) {
        return findAuthenticatedMemberFromRequestAttribute(request)
                .orElseGet(() -> extractAndCacheAuthenticatedMember(request));
    }

    private AuthenticatedMember extractAndCacheAuthenticatedMember(HttpServletRequest request) {
        String token = extractAccessToken(request);
        AuthenticatedMember member = jwtTokenProvider.extractMember(token);
        request.setAttribute(LOGIN_MEMBER_ATTRIBUTE, member);
        return member;
    }

    private Optional<AuthenticatedMember> findAuthenticatedMemberFromRequestAttribute(HttpServletRequest request) {
        Object member = request.getAttribute(LOGIN_MEMBER_ATTRIBUTE);
        if (member instanceof AuthenticatedMember authenticatedMember) {
            return Optional.of(authenticatedMember);
        }
        return Optional.empty();
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }
        return token;
    }

    public AuthenticatedMember getRequiredAuthenticatedMemberFromRequestAttribute(HttpServletRequest request) {
        return findAuthenticatedMemberFromRequestAttribute(request)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.UNAUTHORIZED));
    }

}
