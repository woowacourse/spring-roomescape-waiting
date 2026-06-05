package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import org.springframework.stereotype.Service;
import roomescape.domain.User;
import roomescape.dto.auth.command.LoginCommand;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    public String login(LoginCommand command) {
        String username = command.username();
        String password = command.password();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RoomescapeException(ErrorType.INVALID_LOGIN, "로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요."));

        if (!user.getPassword().matches(password)) {
            throw new RoomescapeException(ErrorType.INVALID_LOGIN, "로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요.");
        }

        return jwtProvider.createToken(user.getId(), user.getUsername(), user.getRole());
    }
}
