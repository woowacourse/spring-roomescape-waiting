package roomescape.service.dto.result;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record MemberResult(
        Long id,
        String name,
        MemberRole role,
        String email
) {
    public static MemberResult from(Member member) {
        return new MemberResult(member.getId(), member.getName(), member.getRole(), member.getEmail());
    }
}
