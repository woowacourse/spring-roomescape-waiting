package roomescape.dto.business;

import roomescape.domain.Role;
import roomescape.domain.User;

public record TokenInfoDto(Long id, Role role) {

    public static TokenInfoDto of(User user) {
        return new TokenInfoDto(user.getId(),
                user.getRole());
    }
}
