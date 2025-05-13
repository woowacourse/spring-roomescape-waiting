package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.infrastructure.jwt.JwtTokenExtractor;
import roomescape.infrastructure.jwt.JwtTokenProvider;

@Component
public class AuthenticatedMemberIdExtractor {
    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticatedMemberIdExtractor(JwtTokenExtractor jwtTokenExtractor, JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public MemberIdDto extract(HttpServletRequest request) {
        String token = jwtTokenExtractor.extract(request);
        String payload = jwtTokenProvider.getPayload(token);
        return new MemberIdDto(Long.parseLong(payload));
    }
}
