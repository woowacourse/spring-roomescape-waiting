package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.User;
import roomescape.dto.request.UserCreationRequest;
import roomescape.dto.response.UserProfileResponse;
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

    public User addUser(UserCreationRequest request) {
        User user = User.createWithoutId(request.role(), request.name(), request.email(), request.password());
        return userRepository.save(user);
    }

    private User loadUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
    }
}
