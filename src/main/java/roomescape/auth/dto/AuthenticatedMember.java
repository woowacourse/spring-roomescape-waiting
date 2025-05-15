package roomescape.auth.dto;

import lombok.Builder;
import roomescape.member.model.Role;

@Builder
public record AuthenticatedMember(
        Long id,
        String email,
        String name,
        Role role
) {
}

