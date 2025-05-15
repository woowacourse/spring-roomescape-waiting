package roomescape.member.controller.response;

import roomescape.member.domain.Member;
import roomescape.member.role.Role;

public record MemberResponse(
        Long id,
        String name,
        String email,
        Role role
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
