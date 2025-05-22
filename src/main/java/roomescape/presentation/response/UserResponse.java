package roomescape.presentation.response;

import roomescape.domain.user.User;

public record UserResponse(
        long id,
        String name
) {

    public static UserResponse from(final User user) {
        return new UserResponse(
                user.id(),
                user.name()
        );
    }
}
