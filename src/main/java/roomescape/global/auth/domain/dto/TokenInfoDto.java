package roomescape.global.auth.domain.dto;

import roomescape.user.domain.Role;
import roomescape.user.domain.User;

public record TokenInfoDto(Long id, Role role) {

    public static TokenInfoDto of(User user) {
        return new TokenInfoDto(user.getId(),
                user.getRole());
    }
}
