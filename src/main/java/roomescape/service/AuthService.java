package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.User;
import roomescape.dto.auth.command.LoginCommand;
import roomescape.exception.InvalidLoginException;
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
                .orElseThrow(InvalidLoginException::new);

        if (!user.getPassword().matches(password)) {
            throw new InvalidLoginException();
        }

        return jwtProvider.createToken(user.getId(), user.getUsername(), user.getRole());
    }
}
