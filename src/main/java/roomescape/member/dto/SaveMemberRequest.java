package roomescape.member.dto;

import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

public record SaveMemberRequest(
        String email,
        String password,
        String name,
        MemberRole role
) {
    public Member toMember(final String encodedPassword) {
        return new Member(
                MemberRole.USER,
                encodedPassword,
                name,
                email
        );
    }
}
