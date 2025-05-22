package roomescape.presentation.support.methodresolver;

import roomescape.domain.member.Role;

public record AuthInfo(
        Long memberId,
        String name,
        Role role
) {
}
