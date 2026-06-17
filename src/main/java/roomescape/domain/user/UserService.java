package roomescape.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JpaUserRepository userRepository;

    public User findOrCreateUser(String name) {
        return userRepository.findByName(name)
            .orElseGet(() -> userRepository.save(User.createWithoutId(name)));
    }
}
