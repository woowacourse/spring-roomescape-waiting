package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import org.springframework.stereotype.Service;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.user.command.CreateUserCommand;
import roomescape.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(CreateUserCommand command) {
        String username = command.username();
        String plainPassword = command.password();
        String name = command.name();

        validatePossibleUsername(username);

        Password hashedPassword = Password.ofEncrypted(plainPassword);
        User user = new User(username, hashedPassword, name, Role.MEMBER);

        Long createdUserId = userRepository.save(user);

        return user.withId(createdUserId);
    }

    private void validatePossibleUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new RoomescapeException(ErrorType.DUPLICATE_USERNAME,
                    "이미 존재하는 username입니다. 다른 username을 입력해주세요.");
        }
    }
}
