package roomescape.member.presentation.dto.response;

import roomescape.member.domain.Member;

public record MemberWebResponse(Long id, String name) {

    public static MemberWebResponse from(final Member member) {
        return new MemberWebResponse(member.getId(), member.getName());
    }
}
