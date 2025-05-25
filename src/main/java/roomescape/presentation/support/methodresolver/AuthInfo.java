package roomescape.presentation.support.methodresolver;

import roomescape.domain.member.MemberRole;

public record AuthInfo(
        Long memberId,
        String name,
        MemberRole role
) {
}
