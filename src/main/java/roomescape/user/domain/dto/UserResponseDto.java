package roomescape.user.domain.dto;

import roomescape.user.domain.User;

public record UserResponseDto(Long id, String roleName, String name, String email, String password) {

    public static UserResponseDto of(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getRole().name(),
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );
    }
}
