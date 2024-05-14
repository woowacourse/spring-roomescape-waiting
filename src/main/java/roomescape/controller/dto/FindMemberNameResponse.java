package roomescape.controller.dto;

import roomescape.domain.member.Member;

public record FindMemberNameResponse(Long id, String name) {

    public static FindMemberNameResponse from(Member member) {
        return new FindMemberNameResponse(
            member.getId(),
            member.getName()
        );
    }
}
