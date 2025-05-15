package roomescape.application.auth.dto;

import roomescape.domain.member.Role;

public record JwtPayload(
        Long memberId,
        String name,
        Role role
) {
}
