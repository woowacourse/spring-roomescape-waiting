package roomescape.member.dto.response;

import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

public record MemberReadResponse(
        Long id,
        String name,
        String email,
        String password,
        RoleType role
) {
    public static MemberReadResponse from(Member member) {
        return new MemberReadResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }
}
