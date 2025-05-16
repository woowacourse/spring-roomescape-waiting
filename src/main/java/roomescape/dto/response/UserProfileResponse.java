package roomescape.dto.response;

import roomescape.domain.User;

public record UserProfileResponse(
        Long id,
        String roleName,
        String name
) {

    public UserProfileResponse(User user) {
        this(user.getId(), user.getRole().toString(), user.getName());
    }
}
