package roomescape.member.dto.response;

import roomescape.member.domain.Member;

public record MemberNameResponse(
    Long id,
    String name
) {

    public static MemberNameResponse from(Member member) {
        return new MemberNameResponse(member.getId(), member.getName());
    }
}
