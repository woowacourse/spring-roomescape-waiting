package roomescape.presentation.response;

import java.util.List;
import roomescape.domain.user.User;

public record UserResponse(
        long id,
        String name
) {

    public static List<UserResponse> fromUsers(final List<User> users) {
        return users.stream()
                .map(UserResponse::fromUser)
                .toList();
    }

    public static UserResponse fromUser(final User user) {
        return new UserResponse(
                user.id(),
                user.name()
        );
    }
}
