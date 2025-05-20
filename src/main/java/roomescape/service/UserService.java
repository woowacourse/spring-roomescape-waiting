package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.User;
import roomescape.dto.business.UserCreationContent;
import roomescape.dto.response.UserProfileResponse;
import roomescape.exception.local.DuplicatedEmailException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(long id) {
        return loadUserById(id);
    }

    public List<UserProfileResponse> findAllUserProfile() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserProfileResponse::new)
                .toList();
    }

    public UserProfileResponse addUser(UserCreationContent request) {
        validateDuplicatedEmail(request.email());
        User user = User.createWithoutId(request.role(), request.name(), request.email(), request.password());
        User savedUser = userRepository.save(user);
        return new UserProfileResponse(savedUser);
    }

    private User loadUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
    }

    private void validateDuplicatedEmail(String email) {
        boolean isDuplicatedEmail = userRepository.existsByEmail(email);
        if (isDuplicatedEmail) {
            throw new DuplicatedEmailException();
        }
    }
}
