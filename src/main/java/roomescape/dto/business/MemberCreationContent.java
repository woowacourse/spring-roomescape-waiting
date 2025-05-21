package roomescape.dto.business;

import roomescape.domain.Role;

public record MemberCreationContent(
        Role role,
        String name,
        String email,
        String password
) {

}
