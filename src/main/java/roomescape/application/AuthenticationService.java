package roomescape.application;

import org.springframework.stereotype.Service;
import roomescape.domain.AuthenticationInfo;
import roomescape.domain.AuthenticationTokenProvider;
import roomescape.domain.User;
import roomescape.domain.repository.UserRepository;

@Service
public class AuthenticationService {

    private final AuthenticationTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthenticationService(final AuthenticationTokenProvider tokenProvider, final UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public String issueToken(final String email, final String password) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new AuthorizationException("이메일 또는 비밀번호가 틀렸습니다."));
        if (!user.matchesPassword(password)) {
            throw new AuthorizationException("이메일 또는 비밀번호가 틀렸습니다.");
        }
        var authenticationInfo = new AuthenticationInfo(user.id(), user.role());
        return tokenProvider.createToken(authenticationInfo);
    }

    public User getUserByToken(final String token) {
        var isValidToken = tokenProvider.isValidToken(token);
        if (!isValidToken) {
            throw new AuthorizationException("토큰이 만료되었거나 유효하지 않습니다.");
        }
        var id = tokenProvider.extractId(token);
        return userRepository.findById(id).orElseThrow(() -> new AuthorizationException("사용자 정보가 없습니다. 다시 로그인 해주세요."));
    }
}
