package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AccessTokenResponse;
import roomescape.exception.global.AuthorizationException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;
import roomescape.utility.JwtTokenProvider;

@Service
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public AccessTokenResponse login(LoginRequest loginRequest) {
        Member member = loadUserByEmail(loginRequest.email());
        validatePasswordForLogin(member, loginRequest.password());
        String accessToken = jwtTokenProvider.createAccessToken(
                new AccessTokenContent(member.getId(), member.getRole(), member.getName()));
        return new AccessTokenResponse(accessToken);
    }

    private Member loadUserByEmail(String email) {
        return userRepository.findOneByEmail(email)
                .orElseThrow(NotFoundUserException::new);
    }

    private void validatePasswordForLogin(Member member, String password) {
        if (!member.isEqualPassword(password)) {
            throw new AuthorizationException("로그인 정보가 올바르지 않습니다.");
        }
    }
}
