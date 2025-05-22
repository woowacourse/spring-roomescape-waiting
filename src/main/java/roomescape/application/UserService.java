package roomescape.application;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(final String email, final String password, final String name) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            throw new AlreadyExistedException("이미 해당 이메일로 가입된 사용자가 있습니다.");
        }

        User user = User.createUser(name, email, password);
        return userRepository.save(user);
    }

    public User getById(final long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + id));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
