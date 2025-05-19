package roomescape.dto.response;

import roomescape.domain.User;
import roomescape.dto.business.AccessTokenContent;

public record UserProfileResponse(
        Long id,
        String roleName,
        String name
) {

    public UserProfileResponse(User user) {
        this(user.getId(), user.getRole().toString(), user.getName());
    }

    public UserProfileResponse(AccessTokenContent accessTokenContent) {
        this(accessTokenContent.id(), accessTokenContent.role().toString(), accessTokenContent.name());
    }
}
