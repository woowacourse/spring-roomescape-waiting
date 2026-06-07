package roomescape.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.application.exception.DuplicateResourceException;
import roomescape.common.security.PasswordEncoder;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.user.UserRole;
import roomescape.application.auth.request.LoginRequest;
import roomescape.application.auth.request.SignupRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User login(LoginRequest request) {
        User user = userRepository.findByName(request.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return user;
    }

    public User signup(SignupRequest request) {
        User user = User.create(request.name(), passwordEncoder.encode(request.password()), UserRole.USER);

        try {
            return userRepository.save(user);
        } catch (DuplicateResourceException exception) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
