package roomescape.member.presentation.response;

import roomescape.member.business.domain.Member;

public record MemberResponse(
        Long id,
        String name
) {

    public static MemberResponse of(final Member member) {
        return new MemberResponse(member.getId(), member.getNameValue());
    }
}
