package roomescape.dto;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record MemberResponse(
        Long id,
        String name,
        String email,
        Role role
) {

    public static MemberResponse from(final Member member) {
        return new MemberResponse(
                member.getId(),
                member.getNameString(),
                member.getEmail(),
                member.getRole()
        );
    }
}
