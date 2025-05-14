package roomescape.member.dto.response;

import roomescape.member.domain.Member;

public record MemberNameSelectResponse(
    Long id,
    String name
) {
    public static MemberNameSelectResponse from(Member member) {
        return new MemberNameSelectResponse(member.getId(), member.getName());
    }
}
