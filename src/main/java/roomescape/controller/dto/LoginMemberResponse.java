package roomescape.controller.dto;

import roomescape.domain.Member;
import roomescape.domain.Role;

public record LoginMemberResponse(
        Long id,
        String loginId,
        String name,
        Role role
) {

    public static LoginMemberResponse from(Member member) {
        return new LoginMemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getRole()
        );
    }
}
