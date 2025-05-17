package roomescape.user.domain.dto;

import roomescape.user.domain.User;

public record UserRequestDto(String role, String name, String email, String password) {

    public User toEntity() {
        return new User(role, name, email, password);
    }
}
