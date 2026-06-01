package roomescape.member.controller.dto.response;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public record MemberDetailDto(
    Long id,
    String name,
    Role role
) {

    public static MemberDetailDto from(Member member) {
        return new MemberDetailDto(member.getId(), member.getName(), member.getRole());
    }
}
