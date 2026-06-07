package roomescape.presentation.auth.response;

import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;

public record LoginResponse(
        Long id,
        String name,
        UserRole role
) {

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}
