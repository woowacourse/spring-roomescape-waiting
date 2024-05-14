package roomescape.member.dto;

import roomescape.auth.Role;

public record LoginMemberInToken(
        Long id,
        Role role,
        String name,
        String email
) {
}
