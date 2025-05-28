package roomescape.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.application.dto.CreateUserRequest;
import roomescape.user.domain.User;
import roomescape.user.domain.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    public User create(final CreateUserRequest request) {
        return userRepository.save(request.toDomain());
    }
}
