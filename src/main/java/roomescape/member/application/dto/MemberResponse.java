package roomescape.member.application.dto;

import roomescape.member.domain.Member;

public record MemberResponse(
        Long id,
        String name
) {

    public static MemberResponse of(final Member member) {
        return new MemberResponse(member.getId(), member.getNameValue());
    }
}
