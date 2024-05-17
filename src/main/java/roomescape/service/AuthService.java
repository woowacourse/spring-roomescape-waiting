package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.dto.TokenResponse;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.service.exception.InvalidTokenException;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(Member member) {
        final String accessToken = jwtTokenProvider
                .generateToken(String.valueOf(member.getId()), member.getRole().name());
        return new TokenResponse(accessToken);
    }

    public Role findMemberRoleByToken(String token) {
        validateToken(token);
        return Role.valueOf(jwtTokenProvider.getRole(token));
    }

    public Long findMemberIdByToken(String token) {
        validateToken(token);
        return Long.valueOf(jwtTokenProvider.getUserId(token));
    }

    public void validateToken(final String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }
}
