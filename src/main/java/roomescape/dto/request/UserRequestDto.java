package roomescape.dto.request;

import roomescape.domain.Role;
import roomescape.domain.User;

public record UserRequestDto(Role role, String name, String email, String password) {

    public User toEntity() {
        return User.createWithoutId(role, name, email, password);
    }
}
