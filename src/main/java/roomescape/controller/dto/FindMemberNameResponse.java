package roomescape.controller.dto;

import roomescape.domain.member.LoginMember;

public record FindMemberNameResponse(Long id, String name) {

    public static FindMemberNameResponse from(LoginMember member) {
        return new FindMemberNameResponse(
            member.getId(),
            member.getName()
        );
    }
}
