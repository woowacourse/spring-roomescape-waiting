package roomescape.business.dto;

import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;

import java.util.List;

public record UserDto(
        Id id,
        UserRole userRole,
        UserName name,
        Email email
) {
    public static UserDto fromEntity(final User user) {
        return new UserDto(
                user.getId(),
                user.getUserRole(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<UserDto> fromEntities(final List<User> users) {
        return users.stream()
                .map(UserDto::fromEntity)
                .toList();
    }
}
