package roomescape.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.application.usecase.CreateUserRequest;
import roomescape.user.domain.User;
import roomescape.user.domain.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    @Override
    public User create(final CreateUserRequest request) {
        return userRepository.save(request.toDomain());
    }
}
