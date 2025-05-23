package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(final String email, final String password, final String name) {
        validateEmailNotRegistered(email);
        User user = User.createUser(name, email, password);

        return userRepository.save(user);
    }

    private void validateEmailNotRegistered(String email) {
        boolean isEmailAlreadyRegistered = userRepository.existsByEmail(email);

        if (isEmailAlreadyRegistered) {
            throw new AlreadyExistedException("이미 해당 이메일로 가입된 사용자가 있습니다.");
        }
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
