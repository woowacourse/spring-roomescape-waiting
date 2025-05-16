package roomescape.dto.business;

import roomescape.domain.Role;

public record UserCreationContent(
        Role role,
        String name,
        String email,
        String password
) {

}
