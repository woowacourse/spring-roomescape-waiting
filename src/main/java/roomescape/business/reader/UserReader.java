package roomescape.business.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.UserDto;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.NotFoundException;

import java.util.List;

import static roomescape.exception.ErrorCode.USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {

    private final Users users;

    public List<UserDto> getAll() {
        List<User> users = this.users.findAll();
        return UserDto.fromEntities(users);
    }

    public UserDto getById(final String userIdValue) {
        User user = users.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        return UserDto.fromEntity(user);
    }
}
