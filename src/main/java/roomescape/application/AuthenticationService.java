package roomescape.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AuthenticationException;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final AuthenticationTokenHandler tokenProvider;
    private final UserRepository userRepository;

    public String issueToken(final String email, final String password) {
        var user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new AuthenticationException("이메일 또는 비밀번호가 틀렸습니다."));
        if (!user.matchesPassword(new Password(password))) {
            throw new AuthenticationException("이메일 또는 비밀번호가 틀렸습니다.");
        }
        var authenticationInfo = new AuthenticationInfo(user.id(), user.role());
        return tokenProvider.createToken(authenticationInfo);
    }

    public User getUserByToken(final String token) {
        var isValidToken = tokenProvider.isValidToken(token);
        if (!isValidToken) {
            throw new AuthenticationException("토큰이 만료되었거나 유효하지 않습니다.");
        }
        var id = tokenProvider.extractId(token);
        return userRepository.findById(id).orElseThrow(() -> new AuthenticationException("사용자 정보가 없습니다. 다시 로그인 해주세요."));
    }
}
