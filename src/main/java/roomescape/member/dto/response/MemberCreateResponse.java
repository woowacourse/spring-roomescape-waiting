package roomescape.member.dto.response;

import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

public record MemberCreateResponse(
        Long id,
        String name,
        String email,
        String password,
        RoleType role
) {
    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }
}
