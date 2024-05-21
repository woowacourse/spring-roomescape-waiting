package roomescape.member.controller.dto.response;

import roomescape.member.domain.Member;

public record MemberNameResponse(
        String name
) {
    public static MemberNameResponse from(final Member member) {
        return new MemberNameResponse(member.getNameValue());
    }
}
