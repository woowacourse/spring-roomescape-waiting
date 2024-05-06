package roomescape.application.member.dto.response;

import roomescape.domain.member.Member;

public record MemberResponse(long id, String name) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }
}
