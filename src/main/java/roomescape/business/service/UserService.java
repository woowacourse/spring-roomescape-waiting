package roomescape.business.service;

import static roomescape.exception.ErrorCode.EMAIL_DUPLICATED;
import static roomescape.exception.ErrorCode.USER_NOT_EXIST;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto register(final String name, final String email, final String password) {
        if (userRepository.existByEmail(email)) {
            throw new InvalidCreateArgumentException(EMAIL_DUPLICATED);
        }
        User user = User.create(name, email, password);
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public UserDto getById(final String userIdValue) {
        User user = userRepository.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        return UserDto.fromEntity(user);
    }

    public UserDto getByEmail(final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        return UserDto.fromEntity(user);
    }

    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return UserDto.fromEntities(users);
    }
}
