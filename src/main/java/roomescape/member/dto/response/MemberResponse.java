package roomescape.member.dto.response;

import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

public record MemberResponse(
        Long id,
        String name,
        String email,
        String password,
        RoleType role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }
}
