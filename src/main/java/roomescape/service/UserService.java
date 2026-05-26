package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.user.CreateUserRequest;
import roomescape.exception.DuplicateUsernameException;
import roomescape.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(CreateUserRequest createUserRequest) {
        String username = createUserRequest.username();
        String plainPassword = createUserRequest.password();
        String name = createUserRequest.name();

        validatePossibleUsername(username);

        Password hashedPassword = Password.ofEncrypted(plainPassword);
        User user = new User(username, hashedPassword, name, Role.MEMBER);

        Long createdUserId = userRepository.save(user);

        return user.withId(createdUserId);
    }

    private void validatePossibleUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException();
        }
    }
}
