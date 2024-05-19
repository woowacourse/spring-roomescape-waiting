package roomescape.controller.reservation.dto;

import roomescape.domain.Member;

public record MemberResponse(String name) {

    public static MemberResponse from(final Member member) {
        return new MemberResponse(member.getName());
    }
}
