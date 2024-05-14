package roomescape.member.controller.dto.response;

import roomescape.member.domain.Member;

public record MemberResponse(
        long id,
        String name
) {
    public static MemberResponse from(final Member member) {
        return new MemberResponse(member.getId(), member.getNameValue());
    }
}
