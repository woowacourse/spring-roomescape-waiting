package roomescape.dto.response;

import roomescape.domain.User;

public record UserResponseDto(
        Long id,
        String roleName,
        String name,
        String email,
        String password
) {

    public UserResponseDto(User user) {
        this(user.getId(), user.getRole().toString(), user.getName(), user.getEmail(), user.getPassword());
    }
}
