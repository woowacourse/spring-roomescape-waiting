package roomescape.user.ui.dto;

import roomescape.user.domain.User;

import java.util.List;

public record UserResponse(Long id, String name, String email) {

    public static UserResponse from(final User user) {
        return new UserResponse(
                user.getId().getValue(),
                user.getName().getValue(),
                user.getEmail().getValue()
        );
    }

    public static List<UserResponse> from(final List<User> users) {
        return users.stream()
                .map(UserResponse::from)
                .toList();
    }

}
