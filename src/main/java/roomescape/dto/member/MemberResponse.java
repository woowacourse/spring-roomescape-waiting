package roomescape.dto.member;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

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
