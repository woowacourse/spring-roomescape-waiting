package roomescape.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import roomescape.domain.Role;
import roomescape.domain.TokenProvider;
import roomescape.exception.AuthenticationException;
import roomescape.service.dto.LoginMember;
import roomescape.service.dto.MemberResponse;

@Service
public class AuthService {

    private static final String TOKEN_NAME = "token";
    private static final String CLAIM_SUB = "sub";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_ROLE = "role";

    private final TokenProvider tokenProvider;

    public AuthService(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public String createToken(MemberResponse memberResponse) {
        Map<String, Object> payload = Map.of(
                CLAIM_SUB, String.valueOf(memberResponse.id()),
                CLAIM_NAME, memberResponse.name(),
                CLAIM_ROLE, memberResponse.role()
        );

        return tokenProvider.createToken(payload);
    }

    public String getTokenName() {
        return TOKEN_NAME;
    }

    public LoginMember findMemberByToken(String token) {
        validateToken(token);
        Map<String, Object> payload = tokenProvider.getPayload(token);

        return new LoginMember(
                Long.parseLong((String) payload.get(CLAIM_SUB)),
                (String) payload.get(CLAIM_NAME),
                Role.valueOf((String) payload.get(CLAIM_ROLE))
        );
    }

    private void validateToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new AuthenticationException();
        }
    }
}
