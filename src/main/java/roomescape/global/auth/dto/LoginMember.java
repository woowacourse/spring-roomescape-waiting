package roomescape.global.auth.dto;

import roomescape.member.entity.RoleType;

public record LoginMember(
        Long id,
        String name,
        RoleType role
) {
}
