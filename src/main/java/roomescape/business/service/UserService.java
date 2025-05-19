package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;

import java.util.List;

import static roomescape.exception.ErrorCode.EMAIL_DUPLICATED;
import static roomescape.exception.ErrorCode.USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto register(final String name, final String email, final String password) {
        if (userRepository.existByEmail(email)) {
            throw new InvalidCreateArgumentException(EMAIL_DUPLICATED);
        }
        val user = User.create(name, email, password);
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public UserDto getById(final String userIdValue) {
        val user = userRepository.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        return UserDto.fromEntity(user);
    }

    public UserDto getByEmail(final String email) {
        val user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        return UserDto.fromEntity(user);
    }

    public List<UserDto> getAll() {
        val users = userRepository.findAll();
        return UserDto.fromEntities(users);
    }
}
