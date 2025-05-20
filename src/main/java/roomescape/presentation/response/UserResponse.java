package roomescape.presentation.response;

import java.util.List;
import roomescape.domain.user.User;

public record UserResponse(
        long id,
        String name
) {

    public static UserResponse from(final User user) {
        return new UserResponse(
                user.id(),
                user.name().value()
        );
    }

    public static List<UserResponse> from(final List<User> users) {
        return users.stream()
                .map(UserResponse::from)
                .toList();
    }
}
