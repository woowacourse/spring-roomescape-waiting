package roomescape.business.application_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.exception.business.InvalidCreateArgumentException;

import static roomescape.exception.ErrorCode.EMAIL_DUPLICATED;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final Users users;

    public UserDto register(final String name, final String email, final String password) {
        if (users.existByEmail(email)) {
            throw new InvalidCreateArgumentException(EMAIL_DUPLICATED);
        }
        User user = User.member(name, email, password);
        users.save(user);
        return UserDto.fromEntity(user);
    }
}
