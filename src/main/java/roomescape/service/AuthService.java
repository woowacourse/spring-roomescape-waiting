package roomescape.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.service.dto.LoginMember;
import roomescape.service.dto.MemberResponse;

@Service
public class AuthService {

    private static final String TOKEN_NAME = "token";
    private static final String CLAIM_SUB = "sub";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_ROLE = "role";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(MemberResponse memberResponse) {
        Map<String, Object> payload = Map.of(
                CLAIM_SUB, String.valueOf(memberResponse.id()),
                CLAIM_NAME, memberResponse.name(),
                CLAIM_ROLE, memberResponse.role()
        );

        return jwtTokenProvider.createToken(payload);
    }

    public String getTokenName() {
        return TOKEN_NAME;
    }

    public LoginMember findMemberByToken(String token) {
        validateToken(token);
        Map<String, Object> payload = jwtTokenProvider.getPayload(token);

        return new LoginMember(
                Long.parseLong((String) payload.get(CLAIM_SUB)),
                (String) payload.get(CLAIM_NAME),
                Role.valueOf((String) payload.get(CLAIM_ROLE))
        );
    }

    private void validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException();
        }
    }
}
