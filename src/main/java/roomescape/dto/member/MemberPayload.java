package roomescape.dto.member;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record MemberPayload(
        String id,
        String name,
        String email,
        Role role
) {

    public static MemberPayload from(Member member) {
        return new MemberPayload(
                member.getId().toString(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
