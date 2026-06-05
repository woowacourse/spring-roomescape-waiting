package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
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
        AuthenticatedMember cachedMember = findAuthenticatedMemberFromRequestAttribute(request);
        if (cachedMember != null) {
            return cachedMember;
        }

        String token = extractAccessToken(request);
        AuthenticatedMember member = jwtTokenProvider.extractMember(token);
        request.setAttribute(LOGIN_MEMBER_ATTRIBUTE, member);
        return member;
    }

    private AuthenticatedMember findAuthenticatedMemberFromRequestAttribute(HttpServletRequest request) {
        Object member = request.getAttribute(LOGIN_MEMBER_ATTRIBUTE);
        if (member instanceof AuthenticatedMember authenticatedMember) {
            return authenticatedMember;
        }
        return null;
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
        AuthenticatedMember member = findAuthenticatedMemberFromRequestAttribute(request);
        if (member == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }
        return member;
    }

}
